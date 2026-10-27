package com.gabriel.backendcontalitros.infrastructure.persistence;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.dados;
import static org.assertj.core.api.Assertions.assertThat;

import com.gabriel.backendcontalitros.domain.model.Leitura;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Os adaptadores contra o esquema real do Flyway (H2 em modo PostgreSQL). */
@SpringBootTest(properties = "contalitro.mqtt.enabled=false")
@Transactional
class PersistenciaAdapterTest {

    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    @Autowired
    private VeiculoPersistenceAdapter veiculos;
    @Autowired
    private ViagemPersistenceAdapter viagens;
    @Autowired
    private LeituraPersistenceAdapter leituras;

    @Test
    void veiculoIdaEVoltaDoBanco() {
        Veiculo salvo = veiculos.salvar(Veiculo.novo("contalitro-001", "ABC1D23", "Caminhao 1", 60.0, 10.0, "tok", T0));

        Veiculo lido = veiculos.buscarPorDeviceId("contalitro-001").orElseThrow();
        assertThat(salvo.getId()).isNotNull();
        assertThat(lido.getCapacidadeTanqueL()).isEqualTo(60.0);
        assertThat(lido.getThingsboardToken()).isEqualTo("tok");
        assertThat(lido.getCriadoEm()).isEqualTo(T0);
        assertThat(veiculos.existePorDeviceId("contalitro-001")).isTrue();
    }

    @Test
    void viagemGuardaBaseETotaisEDeixaDeSerAbertaAoFechar() {
        Long veiculoId = veiculos.salvar(Veiculo.autoCadastrado("contalitro-001", T0)).getId();
        Viagem viagem = viagens.salvar(Viagem.abrir(veiculoId, dados(true, 5.0, 0.5, new BigDecimal("3.10")), T0));

        Viagem aberta = viagens.buscarAbertaPorVeiculo(veiculoId).orElseThrow();
        aberta.atualizar(dados(true, 6.2, 0.6, new BigDecimal("3.85")), T0.plusSeconds(60));
        viagens.salvar(aberta);

        Viagem lida = viagens.buscarPorId(viagem.getId()).orElseThrow();
        assertThat(lida.getBase().custoBrl()).isEqualByComparingTo("3.10");
        assertThat(lida.getTotais().custoBrl()).isEqualByComparingTo("0.75");
        assertThat(viagens.listarAbertasSemLeituraDesde(T0.plusSeconds(61))).hasSize(1);

        lida.fechar(MotivoFechamento.IGNICAO_DESLIGADA, T0.plusSeconds(120));
        viagens.salvar(lida);

        assertThat(viagens.buscarAbertaPorVeiculo(veiculoId)).isEmpty();
        assertThat(viagens.buscarPorId(viagem.getId()).orElseThrow().getMotivoFechamento())
                .isEqualTo(MotivoFechamento.IGNICAO_DESLIGADA);
    }

    @Test
    void leituraDetectaDuplicataEFiltraPorPeriodo() {
        Long veiculoId = veiculos.salvar(Veiculo.autoCadastrado("contalitro-001", T0)).getId();
        String payload = "{\"data\":{\"speed_kmh\":60}}".repeat(200); // maior que um VARCHAR comum
        leituras.salvar(Leitura.registrar(veiculoId, null, T0, T0, dados(true, 1, 0.1, BigDecimal.ONE), payload));

        assertThat(leituras.existe(veiculoId, T0)).isTrue();
        assertThat(leituras.existe(veiculoId, T0.plusSeconds(1))).isFalse();
        assertThat(leituras.listarPorVeiculo(veiculoId, T0, T0.plusSeconds(1)))
                .singleElement().extracting(Leitura::payload).isEqualTo(payload);
        assertThat(leituras.listarPorVeiculo(veiculoId, T0.plusSeconds(1), T0.plusSeconds(2))).isEmpty();
    }
}
