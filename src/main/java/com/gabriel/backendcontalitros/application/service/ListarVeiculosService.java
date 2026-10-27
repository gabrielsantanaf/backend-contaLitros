package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.port.in.ListarVeiculosUseCase;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.util.List;

public class ListarVeiculosService implements ListarVeiculosUseCase {

    private final VeiculoRepositoryPort veiculos;

    public ListarVeiculosService(VeiculoRepositoryPort veiculos) {
        this.veiculos = veiculos;
    }

    @Override
    public List<Veiculo> listar() {
        return veiculos.listarTodos();
    }
}
