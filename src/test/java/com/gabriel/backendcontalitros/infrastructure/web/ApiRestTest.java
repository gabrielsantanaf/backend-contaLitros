package com.gabriel.backendcontalitros.infrastructure.web;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.dados;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gabriel.backendcontalitros.application.port.in.ProcessarTelemetriaUseCase;
import com.gabriel.backendcontalitros.application.port.in.TelemetriaRecebida;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = "contalitro.mqtt.enabled=false")
@AutoConfigureMockMvc
class ApiRestTest {

    private static final String VEICULO_JSON = """
            {
              "deviceId": "contalitro-001",
              "placa": "ABC1D23",
              "modelo": "Caminhao 1",
              "capacidadeTanqueL": 45,
              "metaConsumoKml": 12,
              "thingsboardToken": "token-do-device"
            }
            """;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ProcessarTelemetriaUseCase processarTelemetria;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM leitura");
        jdbc.update("DELETE FROM viagem");
        jdbc.update("DELETE FROM veiculo");
    }

    private ResultActions cadastrar(String json) throws Exception {
        return mvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long cadastrarVeiculo() throws Exception {
        String resposta = cadastrar(VEICULO_JSON).andReturn().getResponse().getContentAsString();
        return Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void cadastraVeiculoSemExporOToken() throws Exception {
        cadastrar(VEICULO_JSON)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.deviceId").value("contalitro-001"))
                .andExpect(jsonPath("$.possuiTokenThingsboard").value(true))
                .andExpect(jsonPath("$.thingsboardToken").doesNotExist())
                .andExpect(content().string(not(containsString("token-do-device"))));

        mvc.perform(get("/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].placa").value("ABC1D23"));
    }

    @Test
    void deviceIdRepetidoRetorna409() throws Exception {
        cadastrar(VEICULO_JSON).andExpect(status().isCreated());

        cadastrar(VEICULO_JSON).andExpect(status().isConflict());
    }

    @Test
    void cadastroInvalidoRetorna400ComOsCampos() throws Exception {
        cadastrar("{\"deviceId\": \"\", \"capacidadeTanqueL\": -1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.deviceId").exists())
                .andExpect(jsonPath("$.campos.capacidadeTanqueL").exists());

        cadastrar("{ nao e json").andExpect(status().isBadRequest());
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/viagens/999")).andExpect(status().isNotFound());
        mvc.perform(get("/veiculos/999/viagens")).andExpect(status().isNotFound());
        mvc.perform(get("/veiculos/999/leituras")).andExpect(status().isNotFound());
    }

    @Test
    void periodoInvalidoRetorna400() throws Exception {
        long id = cadastrarVeiculo();

        mvc.perform(get("/veiculos/{id}/leituras", id).param("de", "ontem"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/veiculos/{id}/leituras", id)
                        .param("de", "2026-10-02T00:00:00Z").param("ate", "2026-10-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultaViagensLeiturasERelatorioDeConsumo() throws Exception {
        long id = cadastrarVeiculo();
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(600);
        enviar(inicio, true, 100.0, 10.0, "50.00");
        enviar(inicio.plusSeconds(60), true, 110.0, 11.0, "56.00");
        enviar(inicio.plusSeconds(120), false, 130.0, 12.0, "62.00");

        String viagens = mvc.perform(get("/veiculos/{id}/viagens", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("FECHADA"))
                .andExpect(jsonPath("$[0].motivoFechamento").value("IGNICAO_DESLIGADA"))
                .andExpect(jsonPath("$[0].distanciaKm").value(30.0))
                .andExpect(jsonPath("$[0].combustivelL").value(2.0))
                .andExpect(jsonPath("$[0].custoBrl").value(12.00))
                .andExpect(jsonPath("$[0].consumoMedioKml").value(15.0))
                .andReturn().getResponse().getContentAsString();
        long viagemId = Long.parseLong(viagens.replaceAll(".*?\"id\":(\\d+).*", "$1"));

        mvc.perform(get("/viagens/{id}", viagemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculoId").value(id));

        mvc.perform(get("/veiculos/{id}/leituras", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].viagemId").value(viagemId))
                .andExpect(jsonPath("$[0].payload").doesNotExist());

        mvc.perform(get("/relatorios/consumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidadeViagens").value(1))
                .andExpect(jsonPath("$[0].consumoMedioKml").value(15.0))
                .andExpect(jsonPath("$[0].consumoAlto").value(false));
    }

    private void enviar(Instant instante, boolean ignicao, double km, double litros, String custo) {
        processarTelemetria.processar(new TelemetriaRecebida("contalitro-001", instante, instante, true,
                dados(ignicao, km, litros, new BigDecimal(custo)), "{}", "{\"data\":{}}"));
    }
}
