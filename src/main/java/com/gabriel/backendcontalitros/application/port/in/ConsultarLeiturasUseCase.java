package com.gabriel.backendcontalitros.application.port.in;

import com.gabriel.backendcontalitros.domain.model.Leitura;
import java.time.Instant;
import java.util.List;

public interface ConsultarLeiturasUseCase {

    /** Leituras do veiculo no periodo [de, ate). Sem periodo informado, a ultima hora. */
    List<Leitura> listar(Long veiculoId, Instant de, Instant ate);
}
