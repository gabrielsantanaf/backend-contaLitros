package com.gabriel.backendcontalitros.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Contadores que o Conta Litro acumula desde que e ligado. Nao voltam a zero
 * quando a ignicao e desligada; so zeram quando o equipamento reinicia.
 */
public record Contadores(
        double distanciaKm,
        double combustivelL,
        BigDecimal custoBrl,
        double tempoOciosoS,
        int aceleracoesBruscas,
        int frenagensBruscas) {

    public static final Contadores ZERO = new Contadores(0, 0, BigDecimal.ZERO, 0, 0, 0);

    public Contadores {
        Objects.requireNonNull(custoBrl, "custoBrl");
        custoBrl = custoBrl.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Recorte dos contadores a partir de uma base: {@code atual - base}.
     * Nunca devolve valores negativos.
     */
    public Contadores desde(Contadores base) {
        return new Contadores(
                Math.max(0, distanciaKm - base.distanciaKm),
                Math.max(0, combustivelL - base.combustivelL),
                custoBrl.subtract(base.custoBrl).max(BigDecimal.ZERO),
                Math.max(0, tempoOciosoS - base.tempoOciosoS),
                Math.max(0, aceleracoesBruscas - base.aceleracoesBruscas),
                Math.max(0, frenagensBruscas - base.frenagensBruscas));
    }
}
