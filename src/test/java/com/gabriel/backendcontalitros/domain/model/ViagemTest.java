package com.gabriel.backendcontalitros.domain.model;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.dados;
import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.ligado;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ViagemTest {

    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void abrirGuardaContadoresDoEquipamentoComoBaseEComecaZerada() {
        Viagem viagem = Viagem.abrir(1L, ligado(5.0), T0);

        assertThat(viagem.estaAberta()).isTrue();
        assertThat(viagem.getBase().distanciaKm()).isEqualTo(5.0);
        assertThat(viagem.getTotais()).isEqualTo(Contadores.ZERO);
        assertThat(viagem.getInicio()).isEqualTo(T0);
    }

    @Test
    void valorDaViagemEAtualMenosBase() {
        Viagem viagem = Viagem.abrir(1L, dados(true, 5.0, 0.5, new BigDecimal("3.00")), T0);

        viagem.atualizar(dados(true, 6.2, 0.62, new BigDecimal("3.72")), T0.plusSeconds(60));

        assertThat(viagem.getTotais().distanciaKm()).isCloseTo(1.2, within(1e-9));
        assertThat(viagem.getTotais().combustivelL()).isCloseTo(0.12, within(1e-9));
        assertThat(viagem.getTotais().custoBrl()).isEqualByComparingTo("0.72");
        assertThat(viagem.getUltimaLeituraEm()).isEqualTo(T0.plusSeconds(60));
    }

    @Test
    void mensagemPerdidaSeCorrigeNaLeituraSeguinte() {
        Viagem viagem = Viagem.abrir(1L, ligado(5.0), T0);

        // a leitura de 6,2 km se perdeu; a seguinte traz o valor absoluto
        viagem.atualizar(ligado(8.0), T0.plusSeconds(120));

        assertThat(viagem.getTotais().distanciaKm()).isCloseTo(3.0, within(1e-9));
    }

    @Test
    void detectaReinicioQuandoDistanciaVoltaParaTras() {
        Viagem viagem = Viagem.abrir(1L, ligado(5.0), T0);

        assertThat(viagem.indicaReinicio(ligado(0.3))).isTrue();
        assertThat(viagem.indicaReinicio(ligado(5.0))).isFalse();
    }

    @Test
    void fecharRegistraMotivoEImpedeNovasAtualizacoes() {
        Viagem viagem = Viagem.abrir(1L, ligado(5.0), T0);

        viagem.fechar(MotivoFechamento.IGNICAO_DESLIGADA, T0.plusSeconds(300));

        assertThat(viagem.getStatus()).isEqualTo(StatusViagem.FECHADA);
        assertThat(viagem.getMotivoFechamento()).isEqualTo(MotivoFechamento.IGNICAO_DESLIGADA);
        assertThat(viagem.getFim()).isEqualTo(T0.plusSeconds(300));
        assertThatThrownBy(() -> viagem.atualizar(ligado(6.0), T0.plusSeconds(360)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void consumoMedioECalculadoESemCombustivelFicaNulo() {
        Viagem viagem = Viagem.abrir(1L, dados(true, 0, 0, BigDecimal.ZERO), T0);
        assertThat(viagem.consumoMedioKml()).isNull();

        viagem.atualizar(dados(true, 12.0, 1.0, BigDecimal.ZERO), T0.plusSeconds(600));
        assertThat(viagem.consumoMedioKml()).isCloseTo(12.0, within(1e-9));
    }
}
