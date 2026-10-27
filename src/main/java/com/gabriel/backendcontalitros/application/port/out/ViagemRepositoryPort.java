package com.gabriel.backendcontalitros.application.port.out;

import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ViagemRepositoryPort {

    Viagem salvar(Viagem viagem);

    Optional<Viagem> buscarPorId(Long id);

    Optional<Viagem> buscarAbertaPorVeiculo(Long veiculoId);

    /** Viagens do veiculo, da mais recente para a mais antiga. */
    List<Viagem> listarPorVeiculo(Long veiculoId);

    /** Viagens abertas cuja ultima leitura e anterior ao limite. */
    List<Viagem> listarAbertasSemLeituraDesde(Instant limite);

    /** Viagens iniciadas em [de, ate). */
    List<Viagem> listarIniciadasEntre(Instant de, Instant ate);
}
