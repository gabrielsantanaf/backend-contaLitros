package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.exception.RecursoNaoEncontradoException;
import com.gabriel.backendcontalitros.application.port.in.ConsultarViagensUseCase;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.util.List;

public class ConsultarViagensService implements ConsultarViagensUseCase {

    private final VeiculoRepositoryPort veiculos;
    private final ViagemRepositoryPort viagens;

    public ConsultarViagensService(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens) {
        this.veiculos = veiculos;
        this.viagens = viagens;
    }

    @Override
    public List<Viagem> listarPorVeiculo(Long veiculoId) {
        if (veiculos.buscarPorId(veiculoId).isEmpty()) {
            throw RecursoNaoEncontradoException.veiculo(veiculoId);
        }
        return viagens.listarPorVeiculo(veiculoId);
    }

    @Override
    public Viagem buscar(Long viagemId) {
        return viagens.buscarPorId(viagemId)
                .orElseThrow(() -> RecursoNaoEncontradoException.viagem(viagemId));
    }
}
