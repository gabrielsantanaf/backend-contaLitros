package com.gabriel.backendcontalitros.application.port.in;

import com.gabriel.backendcontalitros.domain.model.Veiculo;

public interface EditarVeiculoUseCase {

    Veiculo editar(Long veiculoId, AlteracaoVeiculo alteracao);
}
