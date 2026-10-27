package com.gabriel.backendcontalitros.application.port.in;

import java.time.Instant;
import java.util.List;

public interface GerarRelatorioConsumoUseCase {

    /** Consumo por veiculo das viagens iniciadas em [de, ate). Sem periodo, os ultimos 30 dias. */
    List<ConsumoVeiculo> gerar(Instant de, Instant ate);
}
