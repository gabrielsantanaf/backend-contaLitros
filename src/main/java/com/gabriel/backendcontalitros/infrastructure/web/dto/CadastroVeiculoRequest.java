package com.gabriel.backendcontalitros.infrastructure.web.dto;

import com.gabriel.backendcontalitros.application.port.in.NovoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * @param deviceId precisa ser igual ao DEVICE_ID configurado no Conta Litro
 */
public record CadastroVeiculoRequest(
        @NotBlank @Size(max = 64) String deviceId,
        @Size(max = 10) String placa,
        @Size(max = 80) String modelo,
        @Positive Double capacidadeTanqueL,
        @Positive Double metaConsumoKml,
        @Size(max = 120) String thingsboardToken) {

    public NovoVeiculo paraComando() {
        return new NovoVeiculo(deviceId.trim(), placa, modelo, capacidadeTanqueL, metaConsumoKml, thingsboardToken);
    }
}
