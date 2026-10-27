package com.gabriel.backendcontalitros.application.service;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.desligado;
import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.ligado;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.gabriel.backendcontalitros.application.port.in.ResultadoProcessamento;
import com.gabriel.backendcontalitros.application.port.in.TelemetriaRecebida;
import com.gabriel.backendcontalitros.domain.model.DadosTelemetria;
import com.gabriel.backendcontalitros.domain.model.Leitura;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessarTelemetriaServiceTest {

    private static final String DEVICE = "contalitro-001";
    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    private RepositoriosEmMemoria repos;
    private ProcessarTelemetriaService service;

    @BeforeEach
    void setUp() {
        repos = new RepositoriosEmMemoria();
        service = criarService(true);
    }

    private ProcessarTelemetriaService criarService(boolean autoCadastro) {
        return new ProcessarTelemetriaService(repos.veiculos, repos.viagens, repos.leituras,
                repos.painel, repos.transacao, autoCadastro);
    }

    private static TelemetriaRecebida telemetria(DadosTelemetria dados, int segundos) {
        Instant instante = T0.plusSeconds(segundos);
        return new TelemetriaRecebida(DEVICE, instante, instante, true, dados, "{}", "{\"data\":{}}");
    }

    @Test
    void primeiraLeituraComIgnicaoAutoCadastraVeiculoEAbreViagem() {
        ResultadoProcessamento resultado = service.processar(telemetria(ligado(5.0), 0));

        assertThat(resultado).isEqualTo(ResultadoProcessamento.ACEITA);
        Veiculo veiculo = repos.veiculos.buscarPorDeviceId(DEVICE).orElseThrow();
        assertThat(veiculo.isEquipamentoOnline()).isTrue();
        Viagem viagem = repos.viagens.buscarAbertaPorVeiculo(veiculo.getId()).orElseThrow();
        assertThat(repos.leituras.todas()).singleElement()
                .extracting(Leitura::viagemId).isEqualTo(viagem.getId());
    }

    @Test
    void viagemRegistraORecorteDoContador() {
        service.processar(telemetria(ligado(5.0), 0));
        service.processar(telemetria(ligado(6.2), 10));

        Viagem viagem = repos.viagens.todas().getFirst();
        assertThat(viagem.getTotais().distanciaKm()).isCloseTo(1.2, within(1e-9));
    }

    @Test
    void ignicaoDesligadaFechaAViagem() {
        service.processar(telemetria(ligado(5.0), 0));
        service.processar(telemetria(desligado(8.0), 10));

        Viagem viagem = repos.viagens.todas().getFirst();
        assertThat(viagem.estaAberta()).isFalse();
        assertThat(viagem.getMotivoFechamento()).isEqualTo(MotivoFechamento.IGNICAO_DESLIGADA);
        assertThat(viagem.getTotais().distanciaKm()).isEqualTo(3.0);
        assertThat(repos.leituras.todas()).allMatch(l -> viagem.getId().equals(l.viagemId()));
    }

    @Test
    void reinicioDoEquipamentoFechaAViagemEAbreOutra() {
        service.processar(telemetria(ligado(5.0), 0));
        service.processar(telemetria(ligado(0.1), 10));

        assertThat(repos.viagens.todas()).hasSize(2);
        Viagem primeira = repos.viagens.todas().get(0);
        Viagem segunda = repos.viagens.todas().get(1);
        assertThat(primeira.getMotivoFechamento()).isEqualTo(MotivoFechamento.REINICIO_EQUIPAMENTO);
        assertThat(segunda.estaAberta()).isTrue();
        assertThat(repos.leituras.todas().getLast().viagemId()).isEqualTo(segunda.getId());
    }

    @Test
    void descartaQuandoVeiculoEstaOffline() {
        Instant instante = T0;
        TelemetriaRecebida congelada = new TelemetriaRecebida(DEVICE, instante, instante, false,
                ligado(5.0), "{}", "{}");

        assertThat(service.processar(congelada)).isEqualTo(ResultadoProcessamento.DESCARTADA_VEICULO_OFFLINE);
        assertThat(repos.leituras.todas()).isEmpty();
    }

    @Test
    void descartaLeituraDuplicadaDoMesmoEquipamentoEMesmoTs() {
        service.processar(telemetria(ligado(5.0), 0));

        assertThat(service.processar(telemetria(ligado(5.0), 0)))
                .isEqualTo(ResultadoProcessamento.DESCARTADA_DUPLICADA);
        assertThat(repos.leituras.todas()).hasSize(1);
    }

    @Test
    void leituraSemTsNaoEConsideradaDuplicada() {
        TelemetriaRecebida semTs = new TelemetriaRecebida(DEVICE, T0, null, true, ligado(5.0), "{}", "{}");

        service.processar(semTs);

        assertThat(service.processar(semTs)).isEqualTo(ResultadoProcessamento.ACEITA);
    }

    @Test
    void descartaEquipamentoDesconhecidoComAutoCadastroDesligado() {
        service = criarService(false);

        assertThat(service.processar(telemetria(ligado(5.0), 0)))
                .isEqualTo(ResultadoProcessamento.DESCARTADA_VEICULO_DESCONHECIDO);
        assertThat(repos.veiculos.listarTodos()).isEmpty();
    }

    @Test
    void enviaAoPainelSomenteVeiculosComToken() {
        service.processar(telemetria(ligado(5.0), 0));
        assertThat(repos.painel.envios).isEmpty();

        repos.veiculos.salvar(Veiculo.novo("contalitro-002", "ABC1D23", null, null, null, "tok-2", T0));
        Instant instante = T0.plusSeconds(5);
        service.processar(new TelemetriaRecebida("contalitro-002", instante, instante, true,
                ligado(1.0), "{\"speed_kmh\":60}", "{}"));

        assertThat(repos.painel.envios).singleElement().satisfies(envio -> {
            assertThat(envio.token()).isEqualTo("tok-2");
            assertThat(envio.instante()).isEqualTo(instante);
            assertThat(envio.dadosJson()).isEqualTo("{\"speed_kmh\":60}");
        });
    }
}
