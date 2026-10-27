package com.gabriel.backendcontalitros.domain.model;

import java.util.Objects;

/**
 * Valores de uma leitura do Conta Litro: o estado instantaneo do veiculo e os
 * contadores acumulados pelo equipamento.
 */
public record DadosTelemetria(
        double velocidadeKmh,
        int rpm,
        int marcha,
        boolean ignicao,
        double tanquePct,
        double vazaoLph,
        Double consumoKml,
        Double notaEficiencia,
        Contadores contadores) {

    public DadosTelemetria {
        Objects.requireNonNull(contadores, "contadores");
    }
}
