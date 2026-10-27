package com.gabriel.backendcontalitros.infrastructure.web.dto;

import com.gabriel.backendcontalitros.domain.model.Leitura;
import java.time.Instant;

public record LeituraResponse(
        Long id,
        Long viagemId,
        Instant recebidaEm,
        Instant instanteEquipamento,
        double velocidadeKmh,
        int rpm,
        int marcha,
        double tanquePct,
        double vazaoLph,
        Double consumoKml,
        boolean ignicao) {

    public static LeituraResponse de(Leitura l) {
        return new LeituraResponse(l.id(), l.viagemId(), l.recebidaEm(), l.instanteEquipamento(),
                l.velocidadeKmh(), l.rpm(), l.marcha(), l.tanquePct(), l.vazaoLph(), l.consumoKml(), l.ignicao());
    }
}
