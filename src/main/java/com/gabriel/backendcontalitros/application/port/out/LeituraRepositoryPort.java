package com.gabriel.backendcontalitros.application.port.out;

import com.gabriel.backendcontalitros.domain.model.Leitura;
import java.time.Instant;
import java.util.List;

public interface LeituraRepositoryPort {

    Leitura salvar(Leitura leitura);

    boolean existe(Long veiculoId, Instant instanteEquipamento);

    /** Leituras do veiculo recebidas em [de, ate), em ordem cronologica. */
    List<Leitura> listarPorVeiculo(Long veiculoId, Instant de, Instant ate);
}
