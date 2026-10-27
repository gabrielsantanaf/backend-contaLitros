package com.gabriel.backendcontalitros.application.port.in;

import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.util.List;

public interface ConsultarViagensUseCase {

    /** Viagens do veiculo, da mais recente para a mais antiga. */
    List<Viagem> listarPorVeiculo(Long veiculoId);

    Viagem buscar(Long viagemId);
}
