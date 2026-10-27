package com.gabriel.backendcontalitros.infrastructure.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.gabriel.backendcontalitros.application.port.in.FecharViagensInativasUseCase;
import com.gabriel.backendcontalitros.application.port.out.LeituraRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Fluxo completo de uma mensagem, do texto recebido no topico ate o banco. A mensagem
 * e injetada direto no roteador, sem passar pelo cliente Paho.
 */
@SpringBootTest(properties = "contalitro.mqtt.enabled=false")
class FluxoMensagemTest {

    private static final String DEVICE = "contalitro-001";
    private static final String TELEMETRIA = "contalitro/" + DEVICE + "/telemetry";
    private static final String STATUS = "contalitro/" + DEVICE + "/status";

    @Autowired
    private RoteadorMensagens roteador;
    @Autowired
    private FecharViagensInativasUseCase fecharViagensInativas;
    @Autowired
    private VeiculoRepositoryPort veiculos;
    @Autowired
    private ViagemRepositoryPort viagens;
    @Autowired
    private LeituraRepositoryPort leituras;
    @Autowired
    private JdbcTemplate jdbc;

    private Instant t0;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM leitura");
        jdbc.update("DELETE FROM viagem");
        jdbc.update("DELETE FROM veiculo");
        t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static String mensagem(Instant ts, boolean ignicao, double km) {
        return """
                {"ts": %d, "vehicle_online": true,
                 "data": {"speed_kmh": %s, "rpm": 1800, "gear": 3, "ignition": %s, "fuel_level_pct": 60,
                          "fuel_flow_lph": 4.2, "distance_km": %s, "fuel_used_l": %s, "cost_brl": %s}}
                """.formatted(ts.toEpochMilli(), ignicao ? 50 : 0, ignicao, km, km / 12, km / 2);
    }

    private void receber(String topico, String payload, Instant instante) {
        roteador.rotear(topico, payload, instante);
    }

    private Viagem unicaViagem() {
        Long veiculoId = veiculos.buscarPorDeviceId(DEVICE).orElseThrow().getId();
        List<Viagem> doVeiculo = viagens.listarPorVeiculo(veiculoId);
        assertThat(doVeiculo).hasSize(1);
        return doVeiculo.getFirst();
    }

    @Test
    void viagemCompletaDoTextoAteOBanco() {
        receber(TELEMETRIA, mensagem(t0, true, 5.0), t0);
        receber(TELEMETRIA, mensagem(t0.plusSeconds(60), true, 6.2), t0.plusSeconds(60));
        receber(TELEMETRIA, mensagem(t0.plusSeconds(120), false, 7.0), t0.plusSeconds(120));

        Viagem viagem = unicaViagem();
        assertThat(viagem.getMotivoFechamento()).isEqualTo(MotivoFechamento.IGNICAO_DESLIGADA);
        assertThat(viagem.getTotais().distanciaKm()).isCloseTo(2.0, within(1e-9));
        assertThat(viagem.getTotais().custoBrl()).isEqualByComparingTo("1.00");
        Veiculo veiculo = veiculos.buscarPorDeviceId(DEVICE).orElseThrow();
        assertThat(leituras.listarPorVeiculo(veiculo.getId(), t0, t0.plusSeconds(121)))
                .hasSize(3)
                .allMatch(l -> viagem.getId().equals(l.viagemId()))
                .allMatch(l -> l.payload().contains("\"distance_km\""));
    }

    @Test
    void mensagemDuplicadaPorReconexaoNaoEGravadaDuasVezes() {
        receber(TELEMETRIA, mensagem(t0, true, 5.0), t0);
        receber(TELEMETRIA, mensagem(t0, true, 5.0), t0.plusSeconds(2));

        Long veiculoId = veiculos.buscarPorDeviceId(DEVICE).orElseThrow().getId();
        assertThat(leituras.listarPorVeiculo(veiculoId, t0, t0.plusSeconds(10))).hasSize(1);
    }

    @Test
    void lastWillOfflineFechaAViagem() {
        receber(TELEMETRIA, mensagem(t0, true, 5.0), t0);
        receber(STATUS, "offline", t0.plusSeconds(30));

        assertThat(unicaViagem().getMotivoFechamento()).isEqualTo(MotivoFechamento.EQUIPAMENTO_OFFLINE);
        assertThat(veiculos.buscarPorDeviceId(DEVICE).orElseThrow().isEquipamentoOnline()).isFalse();
    }

    @Test
    void viagemSemComunicacaoEFechadaPelaTarefaAgendada() {
        Instant meiaHoraAtras = t0.minusSeconds(1800); // limite de inatividade: 10 min
        receber(TELEMETRIA, mensagem(meiaHoraAtras, true, 5.0), meiaHoraAtras);

        // a tarefa agendada tambem roda em segundo plano e pode ter fechado antes
        fecharViagensInativas.fecharInativas();
        Viagem viagem = unicaViagem();
        assertThat(viagem.getMotivoFechamento()).isEqualTo(MotivoFechamento.SEM_COMUNICACAO);
        assertThat(viagem.getFim()).isEqualTo(meiaHoraAtras);
    }

    @Test
    void mensagemMalformadaETopicoDesconhecidoNaoGravamNada() {
        receber(TELEMETRIA, "{ isto nao e json", t0);
        receber(TELEMETRIA, "{\"ts\": 1790000000000}", t0);
        receber("contalitro/" + DEVICE + "/comando", mensagem(t0, true, 5.0), t0);
        receber("outro/" + DEVICE + "/telemetry", mensagem(t0, true, 5.0), t0);

        assertThat(veiculos.listarTodos()).isEmpty();
    }
}
