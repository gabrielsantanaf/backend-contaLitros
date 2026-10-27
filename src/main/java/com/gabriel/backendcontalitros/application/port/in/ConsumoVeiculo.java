package com.gabriel.backendcontalitros.application.port.in;

import java.math.BigDecimal;

/**
 * Consumo e custo de um veiculo em um periodo.
 *
 * @param consumoMedioKml distancia / combustivel; nulo sem combustivel no periodo
 * @param consumoAlto     true quando o consumo medio ficou abaixo da meta do veiculo
 */
public record ConsumoVeiculo(
        Long veiculoId,
        String deviceId,
        String placa,
        String modelo,
        int quantidadeViagens,
        double distanciaKm,
        double combustivelL,
        BigDecimal custoBrl,
        Double consumoMedioKml,
        Double metaConsumoKml,
        boolean consumoAlto) {
}
