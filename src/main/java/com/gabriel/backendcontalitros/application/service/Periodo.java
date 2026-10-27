package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.exception.PeriodoInvalidoException;
import java.time.Duration;
import java.time.Instant;

/** Periodo de consulta [de, ate), com valores padrao quando os limites nao sao informados. */
record Periodo(Instant de, Instant ate) {

    static Periodo resolver(Instant de, Instant ate, Instant agora, Duration duracaoPadrao) {
        Instant fim = ate != null ? ate : agora;
        Instant inicio = de != null ? de : fim.minus(duracaoPadrao);
        if (!inicio.isBefore(fim)) {
            throw new PeriodoInvalidoException("O parametro 'de' deve ser anterior a 'ate'");
        }
        return new Periodo(inicio, fim);
    }
}
