package com.gabriel.backendcontalitros.application.port.in;

import com.gabriel.backendcontalitros.domain.model.DadosTelemetria;
import java.time.Instant;
import java.util.Objects;

/**
 * Mensagem de telemetria ja convertida pelo adaptador de entrada.
 *
 * @param recebidaEm          relogio do backend no momento da chegada
 * @param instanteEquipamento campo ts do equipamento; nulo quando o NTP falhou
 * @param veiculoOnline       false quando os dados do veiculo estao congelados
 * @param dadosJson           campo data original, repassado sem alteracao ao painel
 * @param payload             mensagem completa original
 */
public record TelemetriaRecebida(
        String deviceId,
        Instant recebidaEm,
        Instant instanteEquipamento,
        boolean veiculoOnline,
        DadosTelemetria dados,
        String dadosJson,
        String payload) {

    public TelemetriaRecebida {
        Objects.requireNonNull(deviceId, "deviceId");
        Objects.requireNonNull(recebidaEm, "recebidaEm");
        Objects.requireNonNull(dados, "dados");
        Objects.requireNonNull(dadosJson, "dadosJson");
        Objects.requireNonNull(payload, "payload");
    }

    /** Instante usado no painel: o do equipamento quando existe, senao o do backend. */
    public Instant instanteReferencia() {
        return instanteEquipamento != null ? instanteEquipamento : recebidaEm;
    }
}
