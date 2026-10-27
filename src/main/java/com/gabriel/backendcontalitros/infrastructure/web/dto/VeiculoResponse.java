package com.gabriel.backendcontalitros.infrastructure.web.dto;

import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.time.Instant;

/** O token do ThingsBoard e segredo: a resposta so informa se ele existe. */
public record VeiculoResponse(
        Long id,
        String deviceId,
        String placa,
        String modelo,
        double capacidadeTanqueL,
        Double metaConsumoKml,
        boolean possuiTokenThingsboard,
        boolean equipamentoOnline,
        Instant ultimaComunicacao,
        Instant criadoEm) {

    public static VeiculoResponse de(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getDeviceId(), v.getPlaca(), v.getModelo(),
                v.getCapacidadeTanqueL(), v.getMetaConsumoKml(), v.possuiTokenPainel(),
                v.isEquipamentoOnline(), v.getUltimaComunicacao(), v.getCriadoEm());
    }
}
