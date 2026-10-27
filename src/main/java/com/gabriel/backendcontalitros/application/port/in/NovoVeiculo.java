package com.gabriel.backendcontalitros.application.port.in;

public record NovoVeiculo(
        String deviceId,
        String placa,
        String modelo,
        Double capacidadeTanqueL,
        Double metaConsumoKml,
        String thingsboardToken) {
}
