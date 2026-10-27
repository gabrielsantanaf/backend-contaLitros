package com.gabriel.backendcontalitros.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentacao OpenAPI exibida pelo Swagger UI em /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI contaLitroOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Conta Litro API")
                .version("0.1")
                .description("""
                        Consulta da telemetria recebida do Conta Litro: veiculos, viagens, leituras e \
                        relatorio de consumo. Periodos (de/ate) em ISO-8601 UTC, ex.: 2026-10-01T00:00:00Z. \
                        Em prod, cadastre o veiculo antes de o equipamento enviar dados."""));
    }
}
