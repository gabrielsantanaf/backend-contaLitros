package com.gabriel.backendcontalitros.application.service;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.dados;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gabriel.backendcontalitros.application.exception.DeviceIdJaCadastradoException;
import com.gabriel.backendcontalitros.application.exception.PeriodoInvalidoException;
import com.gabriel.backendcontalitros.application.exception.RecursoNaoEncontradoException;
import com.gabriel.backendcontalitros.application.port.in.ConsumoVeiculo;
import com.gabriel.backendcontalitros.application.port.in.NovoVeiculo;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultasServiceTest {

    private static final Instant AGORA = Instant.parse("2026-10-20T12:00:00Z");

    private RepositoriosEmMemoria repos;
    private Clock relogio;

    @BeforeEach
    void setUp() {
        repos = new RepositoriosEmMemoria();
        relogio = Clock.fixed(AGORA, ZoneOffset.UTC);
    }

    @Test
    void cadastroRecusaDeviceIdRepetido() {
        CadastrarVeiculoService service = new CadastrarVeiculoService(repos.veiculos, repos.transacao, relogio);
        NovoVeiculo novo = new NovoVeiculo("contalitro-001", "ABC1D23", "Caminhao 1", 45.0, 10.0, null);

        Veiculo cadastrado = service.cadastrar(novo);

        assertThat(cadastrado.getId()).isNotNull();
        assertThat(cadastrado.getCriadoEm()).isEqualTo(AGORA);
        assertThatThrownBy(() -> service.cadastrar(novo)).isInstanceOf(DeviceIdJaCadastradoException.class);
    }

    @Test
    void consultaDeVeiculoInexistenteLancaNaoEncontrado() {
        ConsultarViagensService viagens = new ConsultarViagensService(repos.veiculos, repos.viagens);
        ConsultarLeiturasService leituras = new ConsultarLeiturasService(repos.veiculos, repos.leituras, relogio);

        assertThatThrownBy(() -> viagens.listarPorVeiculo(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> viagens.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> leituras.listar(99L, null, null)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void periodoComInicioDepoisDoFimEInvalido() {
        ConsultarLeiturasService leituras = new ConsultarLeiturasService(repos.veiculos, repos.leituras, relogio);

        assertThatThrownBy(() -> leituras.listar(1L, AGORA, AGORA.minusSeconds(60)))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    void relatorioSomaViagensDoPeriodoECalculaConsumoMedio() {
        Veiculo veiculo = repos.veiculos.salvar(Veiculo.novo("contalitro-001", "ABC1D23", null, null, 12.0, null, AGORA));
        Veiculo parado = repos.veiculos.salvar(Veiculo.novo("contalitro-002", null, null, null, null, null, AGORA));
        criarViagem(veiculo, AGORA.minusSeconds(3600), 50, 5, "30.00");
        criarViagem(veiculo, AGORA.minusSeconds(7200), 30, 3, "18.50");
        criarViagem(veiculo, AGORA.minusSeconds(40L * 24 * 3600), 999, 1, "1.00"); // fora dos 30 dias

        GerarRelatorioConsumoService service = new GerarRelatorioConsumoService(repos.veiculos, repos.viagens, relogio);
        List<ConsumoVeiculo> relatorio = service.gerar(null, null);

        assertThat(relatorio).hasSize(2);
        ConsumoVeiculo consumo = relatorio.getFirst();
        assertThat(consumo.quantidadeViagens()).isEqualTo(2);
        assertThat(consumo.distanciaKm()).isEqualTo(80.0);
        assertThat(consumo.combustivelL()).isEqualTo(8.0);
        assertThat(consumo.custoBrl()).isEqualByComparingTo("48.50");
        assertThat(consumo.consumoMedioKml()).isEqualTo(10.0);
        assertThat(consumo.consumoAlto()).isTrue(); // 10 km/l abaixo da meta de 12
        assertThat(relatorio.get(1).consumoMedioKml()).isNull();
        assertThat(relatorio.get(1).quantidadeViagens()).isZero();
    }

    private void criarViagem(Veiculo veiculo, Instant inicio, double km, double litros, String custo) {
        Viagem viagem = Viagem.abrir(veiculo.getId(), dados(true, 0, 0, BigDecimal.ZERO), inicio);
        viagem.atualizar(dados(true, km, litros, new BigDecimal(custo)), inicio.plusSeconds(600));
        viagem.fechar(MotivoFechamento.IGNICAO_DESLIGADA, inicio.plusSeconds(600));
        repos.viagens.salvar(viagem);
    }
}
