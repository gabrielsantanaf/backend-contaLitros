package com.gabriel.backendcontalitros.infrastructure.web;

import com.gabriel.backendcontalitros.application.port.in.ConsumoVeiculo;
import com.gabriel.backendcontalitros.application.port.in.GerarRelatorioConsumoUseCase;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final GerarRelatorioConsumoUseCase gerarRelatorioConsumo;

    public RelatorioController(GerarRelatorioConsumoUseCase gerarRelatorioConsumo) {
        this.gerarRelatorioConsumo = gerarRelatorioConsumo;
    }

    /** Consumo e custo por veiculo no periodo [de, ate) em ISO-8601 UTC. Padrao: ultimos 30 dias. */
    @GetMapping("/consumo")
    public List<ConsumoVeiculo> consumo(@RequestParam(required = false) Instant de,
                                        @RequestParam(required = false) Instant ate) {
        return gerarRelatorioConsumo.gerar(de, ate);
    }
}
