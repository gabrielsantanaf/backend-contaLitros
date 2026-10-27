package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.exception.RecursoNaoEncontradoException;
import com.gabriel.backendcontalitros.application.port.in.ConsultarLeiturasUseCase;
import com.gabriel.backendcontalitros.application.port.out.LeituraRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Leitura;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ConsultarLeiturasService implements ConsultarLeiturasUseCase {

    static final Duration PERIODO_PADRAO = Duration.ofHours(1);

    private final VeiculoRepositoryPort veiculos;
    private final LeituraRepositoryPort leituras;
    private final Clock relogio;

    public ConsultarLeiturasService(VeiculoRepositoryPort veiculos, LeituraRepositoryPort leituras, Clock relogio) {
        this.veiculos = veiculos;
        this.leituras = leituras;
        this.relogio = relogio;
    }

    @Override
    public List<Leitura> listar(Long veiculoId, Instant de, Instant ate) {
        Periodo periodo = Periodo.resolver(de, ate, relogio.instant(), PERIODO_PADRAO);
        if (veiculos.buscarPorId(veiculoId).isEmpty()) {
            throw RecursoNaoEncontradoException.veiculo(veiculoId);
        }
        return leituras.listarPorVeiculo(veiculoId, periodo.de(), periodo.ate());
    }
}
