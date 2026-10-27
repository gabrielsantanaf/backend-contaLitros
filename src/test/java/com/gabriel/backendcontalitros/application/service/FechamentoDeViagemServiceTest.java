package com.gabriel.backendcontalitros.application.service;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.ligado;
import static org.assertj.core.api.Assertions.assertThat;

import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Fechamentos que nao dependem de uma leitura: Last Will e falta de comunicacao. */
class FechamentoDeViagemServiceTest {

    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    private RepositoriosEmMemoria repos;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        repos = new RepositoriosEmMemoria();
        veiculo = repos.veiculos.salvar(Veiculo.autoCadastrado("contalitro-001", T0));
        veiculo.registrarComunicacao(T0);
    }

    @Test
    void statusOfflineFechaViagemEMarcaEquipamentoOffline() {
        Viagem viagem = repos.viagens.salvar(Viagem.abrir(veiculo.getId(), ligado(5.0), T0));
        ProcessarStatusService service = new ProcessarStatusService(repos.veiculos, repos.viagens, repos.transacao);

        boolean conhecido = service.processar("contalitro-001", false, T0.plusSeconds(30));

        assertThat(conhecido).isTrue();
        assertThat(viagem.getMotivoFechamento()).isEqualTo(MotivoFechamento.EQUIPAMENTO_OFFLINE);
        assertThat(viagem.getFim()).isEqualTo(T0.plusSeconds(30));
        assertThat(veiculo.isEquipamentoOnline()).isFalse();
    }

    @Test
    void statusDeEquipamentoDesconhecidoEIgnorado() {
        ProcessarStatusService service = new ProcessarStatusService(repos.veiculos, repos.viagens, repos.transacao);

        assertThat(service.processar("desconhecido", false, T0)).isFalse();
    }

    @Test
    void fechaSomenteViagensSemLeituraAlemDoLimite() {
        Veiculo outro = repos.veiculos.salvar(Veiculo.autoCadastrado("contalitro-002", T0));
        Viagem antiga = repos.viagens.salvar(Viagem.abrir(veiculo.getId(), ligado(5.0), T0));
        Viagem recente = repos.viagens.salvar(Viagem.abrir(outro.getId(), ligado(5.0), T0.plusSeconds(9 * 60)));
        Clock agora = Clock.fixed(T0.plusSeconds(11 * 60), ZoneOffset.UTC);
        FecharViagensInativasService service = new FecharViagensInativasService(
                repos.viagens, repos.transacao, agora, Duration.ofMinutes(10));

        assertThat(service.fecharInativas()).isEqualTo(1);
        assertThat(antiga.getMotivoFechamento()).isEqualTo(MotivoFechamento.SEM_COMUNICACAO);
        assertThat(antiga.getFim()).isEqualTo(T0);
        assertThat(recente.estaAberta()).isTrue();
    }
}
