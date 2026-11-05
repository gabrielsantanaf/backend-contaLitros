package com.gabriel.backendcontalitros.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gabriel.backendcontalitros.application.exception.RecursoNaoEncontradoException;
import com.gabriel.backendcontalitros.application.port.in.AlteracaoVeiculo;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditarVeiculoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-10-20T12:00:00Z");

    private RepositoriosEmMemoria repos;
    private EditarVeiculoService service;
    private Long veiculoId;

    @BeforeEach
    void setUp() {
        repos = new RepositoriosEmMemoria();
        service = new EditarVeiculoService(repos.veiculos, repos.transacao);
        veiculoId = repos.veiculos.salvar(
                Veiculo.novo("contalitro-001", "ABC1D23", "Caminhao 1", 45.0, 10.0, null, AGORA)).getId();
    }

    @Test
    void cadastraTokenDepoisSemMexerNosOutrosCampos() {
        Veiculo editado = service.editar(veiculoId,
                new AlteracaoVeiculo(null, null, null, null, "  token-do-device  "));

        assertThat(editado.getThingsboardToken()).isEqualTo("token-do-device");
        assertThat(editado.getPlaca()).isEqualTo("ABC1D23");
        assertThat(editado.getModelo()).isEqualTo("Caminhao 1");
        assertThat(editado.getCapacidadeTanqueL()).isEqualTo(45.0);
        assertThat(editado.getMetaConsumoKml()).isEqualTo(10.0);
        assertThat(repos.veiculos.buscarPorId(veiculoId)).get()
                .extracting(Veiculo::possuiTokenPainel).isEqualTo(true);
    }

    @Test
    void alteraOsCamposInformadosERemoveTextoVazio() {
        service.editar(veiculoId, new AlteracaoVeiculo(null, null, null, null, "token-do-device"));

        Veiculo editado = service.editar(veiculoId, new AlteracaoVeiculo("XYZ9A87", "", 60.0, 12.0, ""));

        assertThat(editado.getPlaca()).isEqualTo("XYZ9A87");
        assertThat(editado.getModelo()).isNull();
        assertThat(editado.getCapacidadeTanqueL()).isEqualTo(60.0);
        assertThat(editado.getMetaConsumoKml()).isEqualTo(12.0);
        assertThat(editado.possuiTokenPainel()).isFalse();
    }

    @Test
    void veiculoInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> service.editar(99L, new AlteracaoVeiculo(null, null, null, null, "token")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
