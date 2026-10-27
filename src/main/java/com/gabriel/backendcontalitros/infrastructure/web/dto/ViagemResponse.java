package com.gabriel.backendcontalitros.infrastructure.web.dto;

import com.gabriel.backendcontalitros.domain.model.Contadores;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.StatusViagem;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.math.BigDecimal;
import java.time.Instant;

public record ViagemResponse(
        Long id,
        Long veiculoId,
        StatusViagem status,
        Instant inicio,
        Instant fim,
        MotivoFechamento motivoFechamento,
        double distanciaKm,
        double combustivelL,
        BigDecimal custoBrl,
        double tempoOciosoS,
        int aceleracoesBruscas,
        int frenagensBruscas,
        Double notaEficiencia,
        Double consumoMedioKml) {

    public static ViagemResponse de(Viagem v) {
        Contadores t = v.getTotais();
        return new ViagemResponse(v.getId(), v.getVeiculoId(), v.getStatus(), v.getInicio(), v.getFim(),
                v.getMotivoFechamento(), t.distanciaKm(), t.combustivelL(), t.custoBrl(), t.tempoOciosoS(),
                t.aceleracoesBruscas(), t.frenagensBruscas(), v.getNotaEficiencia(), v.consumoMedioKml());
    }
}
