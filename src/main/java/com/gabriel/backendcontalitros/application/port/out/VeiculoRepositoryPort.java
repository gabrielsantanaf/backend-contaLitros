package com.gabriel.backendcontalitros.application.port.out;

import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepositoryPort {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(Long id);

    Optional<Veiculo> buscarPorDeviceId(String deviceId);

    boolean existePorDeviceId(String deviceId);

    List<Veiculo> listarTodos();
}
