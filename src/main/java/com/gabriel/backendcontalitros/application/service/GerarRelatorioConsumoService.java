package com.gabriel.backendcontalitros.application.service;

import static java.util.stream.Collectors.groupingBy;

import com.gabriel.backendcontalitros.application.port.in.ConsumoVeiculo;
import com.gabriel.backendcontalitros.application.port.in.GerarRelatorioConsumoUseCase;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Consumo e custo por veiculo. O consumo medio e calculado na consulta
 * (distancia / combustivel), nunca armazenado.
 */
public class GerarRelatorioConsumoService implements GerarRelatorioConsumoUseCase {

    static final Duration PERIODO_PADRAO = Duration.ofDays(30);

    private final VeiculoRepositoryPort veiculos;
    private final ViagemRepositoryPort viagens;
    private final Clock relogio;

    public GerarRelatorioConsumoService(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens, Clock relogio) {
        this.veiculos = veiculos;
        this.viagens = viagens;
        this.relogio = relogio;
    }

    @Override
    public List<ConsumoVeiculo> gerar(Instant de, Instant ate) {
        Periodo periodo = Periodo.resolver(de, ate, relogio.instant(), PERIODO_PADRAO);
        Map<Long, List<Viagem>> viagensPorVeiculo = viagens.listarIniciadasEntre(periodo.de(), periodo.ate())
                .stream()
                .collect(groupingBy(Viagem::getVeiculoId));

        return veiculos.listarTodos().stream()
                .map(veiculo -> consolidar(veiculo, viagensPorVeiculo.getOrDefault(veiculo.getId(), List.of())))
                .toList();
    }

    private ConsumoVeiculo consolidar(Veiculo veiculo, List<Viagem> viagensDoVeiculo) {
        double distancia = 0;
        double combustivel = 0;
        BigDecimal custo = BigDecimal.ZERO.setScale(2);
        for (Viagem viagem : viagensDoVeiculo) {
            distancia += viagem.getTotais().distanciaKm();
            combustivel += viagem.getTotais().combustivelL();
            custo = custo.add(viagem.getTotais().custoBrl());
        }
        Double consumoMedio = combustivel > 0 ? distancia / combustivel : null;
        Double meta = veiculo.getMetaConsumoKml();
        boolean consumoAlto = meta != null && consumoMedio != null && consumoMedio < meta;

        return new ConsumoVeiculo(veiculo.getId(), veiculo.getDeviceId(), veiculo.getPlaca(), veiculo.getModelo(),
                viagensDoVeiculo.size(), distancia, combustivel, custo, consumoMedio, meta, consumoAlto);
    }
}
