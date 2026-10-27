package com.gabriel.backendcontalitros.infrastructure.web.dto;

import com.gabriel.backendcontalitros.application.port.in.NovoVeiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CadastroVeiculoRequest(
        @Schema(description = "Igual ao DEVICE_ID configurado no Conta Litro", example = "contalitro-001")
        @NotBlank @Size(max = 64) String deviceId,
        @Schema(example = "ABC1D23")
        @Size(max = 10) String placa,
        @Schema(example = "Caminhao 1")
        @Size(max = 80) String modelo,
        @Schema(description = "Padrao: 45", example = "45")
        @Positive Double capacidadeTanqueL,
        @Schema(description = "Abaixo disso o consumo e considerado alto", example = "10")
        @Positive Double metaConsumoKml,
        @Schema(description = "Token do dispositivo no ThingsBoard (opcional, nunca volta nas respostas)",
                example = "token-do-device")
        @Size(max = 120) String thingsboardToken) {

    public NovoVeiculo paraComando() {
        return new NovoVeiculo(deviceId.trim(), placa, modelo, capacidadeTanqueL, metaConsumoKml, thingsboardToken);
    }
}
