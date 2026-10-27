package com.gabriel.backendcontalitros.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Uma mensagem de telemetria aceita.
 *
 * @param recebidaEm          relogio do backend; sempre existe
 * @param instanteEquipamento relogio do ESP32; nulo quando o NTP falhou
 * @param payload             mensagem original, guardada para reprocessamento
 */
public record Leitura(
        Long id,
        Long veiculoId,
        Long viagemId,
        Instant recebidaEm,
        Instant instanteEquipamento,
        double velocidadeKmh,
        int rpm,
        int marcha,
        double tanquePct,
        double vazaoLph,
        Double consumoKml,
        boolean ignicao,
        String payload) {

    public Leitura {
        Objects.requireNonNull(veiculoId, "veiculoId");
        Objects.requireNonNull(recebidaEm, "recebidaEm");
        Objects.requireNonNull(payload, "payload");
    }

    public static Leitura registrar(Long veiculoId, Long viagemId, Instant recebidaEm,
                                    Instant instanteEquipamento, DadosTelemetria dados, String payload) {
        return new Leitura(null, veiculoId, viagemId, recebidaEm, instanteEquipamento,
                dados.velocidadeKmh(), dados.rpm(), dados.marcha(), dados.tanquePct(),
                dados.vazaoLph(), dados.consumoKml(), dados.ignicao(), payload);
    }
}
