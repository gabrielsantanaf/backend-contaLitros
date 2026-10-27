package com.gabriel.backendcontalitros.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class VeiculoTest {

    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void autoCadastradoUsaCapacidadePadraoESemToken() {
        Veiculo veiculo = Veiculo.autoCadastrado("contalitro-001", T0);

        assertThat(veiculo.getCapacidadeTanqueL()).isEqualTo(Veiculo.CAPACIDADE_TANQUE_PADRAO_L);
        assertThat(veiculo.possuiTokenPainel()).isFalse();
        assertThat(veiculo.isEquipamentoOnline()).isFalse();
    }

    @Test
    void registraComunicacaoEMarcaOffline() {
        Veiculo veiculo = Veiculo.autoCadastrado("contalitro-001", T0);

        veiculo.registrarComunicacao(T0.plusSeconds(10));
        assertThat(veiculo.isEquipamentoOnline()).isTrue();
        assertThat(veiculo.getUltimaComunicacao()).isEqualTo(T0.plusSeconds(10));

        veiculo.marcarOffline();
        assertThat(veiculo.isEquipamentoOnline()).isFalse();
    }

    @Test
    void exigeDeviceId() {
        assertThatThrownBy(() -> Veiculo.autoCadastrado(" ", T0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
