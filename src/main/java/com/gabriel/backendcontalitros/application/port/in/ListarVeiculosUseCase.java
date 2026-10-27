package com.gabriel.backendcontalitros.application.port.in;

import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.util.List;

public interface ListarVeiculosUseCase {

    List<Veiculo> listar();
}
