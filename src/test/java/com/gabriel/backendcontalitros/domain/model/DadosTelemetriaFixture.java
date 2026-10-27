package com.gabriel.backendcontalitros.domain.model;

import java.math.BigDecimal;

/** Cria leituras de teste variando so o que importa para as regras de viagem. */
public final class DadosTelemetriaFixture {

    private DadosTelemetriaFixture() {
    }

    public static DadosTelemetria ligado(double distanciaKm) {
        return dados(true, distanciaKm, distanciaKm / 10, BigDecimal.valueOf(distanciaKm * 0.6));
    }

    public static DadosTelemetria desligado(double distanciaKm) {
        return dados(false, distanciaKm, distanciaKm / 10, BigDecimal.valueOf(distanciaKm * 0.6));
    }

    public static DadosTelemetria dados(boolean ignicao, double distanciaKm, double combustivelL, BigDecimal custoBrl) {
        Contadores contadores = new Contadores(distanciaKm, combustivelL, custoBrl, 0, 0, 0);
        return new DadosTelemetria(ignicao ? 60 : 0, ignicao ? 2000 : 0, ignicao ? 4 : 0, ignicao,
                70, ignicao ? 6 : 0, ignicao ? 10.0 : null, 80.0, contadores);
    }
}
