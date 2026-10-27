package com.gabriel.backendcontalitros.application.port.in;

import com.gabriel.backendcontalitros.domain.model.Veiculo;

public interface CadastrarVeiculoUseCase {

    Veiculo cadastrar(NovoVeiculo novoVeiculo);
}
