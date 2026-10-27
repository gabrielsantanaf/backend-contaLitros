package com.gabriel.backendcontalitros.domain.model;

import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.desligado;
import static com.gabriel.backendcontalitros.domain.model.DadosTelemetriaFixture.ligado;
import static org.assertj.core.api.Assertions.assertThat;

import com.gabriel.backendcontalitros.domain.model.RegrasDeViagem.Transicao;
import com.gabriel.backendcontalitros.domain.model.RegrasDeViagem.VinculoLeitura;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RegrasDeViagemTest {

    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void ignicaoLigadaSemViagemAbreUmaNova() {
        Transicao t = RegrasDeViagem.aplicar(1L, null, ligado(5.0), T0);

        assertThat(t.fechada()).isNull();
        assertThat(t.aberta()).isNotNull();
        assertThat(t.aberta().getBase().distanciaKm()).isEqualTo(5.0);
        assertThat(t.vinculo()).isEqualTo(VinculoLeitura.VIAGEM_ABERTA);
    }

    @Test
    void ignicaoLigadaComViagemAbertaAtualizaORecorte() {
        Viagem aberta = Viagem.abrir(1L, ligado(5.0), T0);

        Transicao t = RegrasDeViagem.aplicar(1L, aberta, ligado(7.0), T0.plusSeconds(60));

        assertThat(t.aberta()).isSameAs(aberta);
        assertThat(aberta.getTotais().distanciaKm()).isEqualTo(2.0);
    }

    @Test
    void ignicaoDesligadaFechaAViagemComALeituraFinal() {
        Viagem aberta = Viagem.abrir(1L, ligado(5.0), T0);

        Transicao t = RegrasDeViagem.aplicar(1L, aberta, desligado(9.0), T0.plusSeconds(60));

        assertThat(t.fechada()).isSameAs(aberta);
        assertThat(t.aberta()).isNull();
        assertThat(aberta.getMotivoFechamento()).isEqualTo(MotivoFechamento.IGNICAO_DESLIGADA);
        assertThat(aberta.getTotais().distanciaKm()).isEqualTo(4.0);
        assertThat(t.vinculo()).isEqualTo(VinculoLeitura.VIAGEM_FECHADA);
    }

    @Test
    void reinicioDoEquipamentoFechaAViagemEAbreOutra() {
        Viagem aberta = Viagem.abrir(1L, ligado(5.0), T0);

        Transicao t = RegrasDeViagem.aplicar(1L, aberta, ligado(0.2), T0.plusSeconds(60));

        assertThat(t.fechada()).isSameAs(aberta);
        assertThat(aberta.getMotivoFechamento()).isEqualTo(MotivoFechamento.REINICIO_EQUIPAMENTO);
        assertThat(t.aberta()).isNotNull().isNotSameAs(aberta);
        assertThat(t.aberta().getBase().distanciaKm()).isEqualTo(0.2);
        assertThat(t.vinculo()).isEqualTo(VinculoLeitura.VIAGEM_ABERTA);
    }

    @Test
    void ignicaoDesligadaSemViagemNaoFazNada() {
        Transicao t = RegrasDeViagem.aplicar(1L, null, desligado(5.0), T0);

        assertThat(t.fechada()).isNull();
        assertThat(t.aberta()).isNull();
        assertThat(t.vinculo()).isEqualTo(VinculoLeitura.NENHUM);
    }
}
