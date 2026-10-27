package com.gabriel.backendcontalitros.infrastructure.web;

import com.gabriel.backendcontalitros.application.port.in.ConsumoVeiculo;
import com.gabriel.backendcontalitros.application.port.in.GerarRelatorioConsumoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Relatorios")
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final GerarRelatorioConsumoUseCase gerarRelatorioConsumo;

    public RelatorioController(GerarRelatorioConsumoUseCase gerarRelatorioConsumo) {
        this.gerarRelatorioConsumo = gerarRelatorioConsumo;
    }

    @Operation(summary = "Consumo e custo por veiculo",
            description = "Soma as viagens iniciadas no periodo [de, ate). Padrao: ultimos 30 dias. "
                    + "consumoAlto indica consumo medio abaixo da meta do veiculo.")
    @GetMapping("/consumo")
    public List<ConsumoVeiculo> consumo(@Parameter(description = "Inicio, ISO-8601 UTC", example = "2026-10-01T00:00:00Z")
                                        @RequestParam(required = false) Instant de,
                                        @Parameter(description = "Fim (exclusivo), ISO-8601 UTC", example = "2026-11-01T00:00:00Z")
                                        @RequestParam(required = false) Instant ate) {
        return gerarRelatorioConsumo.gerar(de, ate);
    }
}
