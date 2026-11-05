package com.gabriel.backendcontalitros.infrastructure.web.dto;

import com.gabriel.backendcontalitros.application.port.in.AlteracaoVeiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Envie so os campos que mudam. Campo ausente ou nulo mantem o valor atual; "
        + "texto vazio (\"\") remove placa, modelo ou token.")
public record EdicaoVeiculoRequest(
        @Schema(example = "ABC1D23")
        @Size(max = 10) String placa,
        @Schema(example = "Caminhao 1")
        @Size(max = 80) String modelo,
        @Schema(example = "45")
        @Positive Double capacidadeTanqueL,
        @Schema(description = "Abaixo disso o consumo e considerado alto", example = "10")
        @Positive Double metaConsumoKml,
        @Schema(description = "Token do dispositivo no ThingsBoard (nunca volta nas respostas)",
                example = "token-do-device")
        @Size(max = 120) String thingsboardToken) {

    public AlteracaoVeiculo paraComando() {
        return new AlteracaoVeiculo(placa, modelo, capacidadeTanqueL, metaConsumoKml, thingsboardToken);
    }
}
