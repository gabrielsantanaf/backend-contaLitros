package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.exception.RecursoNaoEncontradoException;
import com.gabriel.backendcontalitros.application.port.in.AlteracaoVeiculo;
import com.gabriel.backendcontalitros.application.port.in.EditarVeiculoUseCase;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Veiculo;

public class EditarVeiculoService implements EditarVeiculoUseCase {

    private final VeiculoRepositoryPort veiculos;
    private final TransacaoPort transacao;

    public EditarVeiculoService(VeiculoRepositoryPort veiculos, TransacaoPort transacao) {
        this.veiculos = veiculos;
        this.transacao = transacao;
    }

    @Override
    public Veiculo editar(Long veiculoId, AlteracaoVeiculo alteracao) {
        return transacao.executar(() -> {
            Veiculo veiculo = veiculos.buscarPorId(veiculoId)
                    .orElseThrow(() -> RecursoNaoEncontradoException.veiculo(veiculoId));
            veiculo.alterarCadastro(
                    texto(alteracao.placa(), veiculo.getPlaca()),
                    texto(alteracao.modelo(), veiculo.getModelo()),
                    alteracao.capacidadeTanqueL() != null
                            ? alteracao.capacidadeTanqueL() : veiculo.getCapacidadeTanqueL(),
                    alteracao.metaConsumoKml() != null ? alteracao.metaConsumoKml() : veiculo.getMetaConsumoKml(),
                    texto(alteracao.thingsboardToken(), veiculo.getThingsboardToken()));
            return veiculos.salvar(veiculo);
        });
    }

    /** Nulo mantem o valor atual; vazio remove; o resto chega sem espacos nas pontas. */
    private static String texto(String novo, String atual) {
        if (novo == null) {
            return atual;
        }
        return novo.isBlank() ? null : novo.strip();
    }
}
