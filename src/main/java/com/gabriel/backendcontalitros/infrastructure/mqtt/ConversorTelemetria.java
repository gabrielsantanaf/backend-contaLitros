package com.gabriel.backendcontalitros.infrastructure.mqtt;

import com.gabriel.backendcontalitros.application.port.in.TelemetriaRecebida;
import com.gabriel.backendcontalitros.domain.model.Contadores;
import com.gabriel.backendcontalitros.domain.model.DadosTelemetria;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrato de entrada: converte o texto publicado pelo Conta Litro em objetos da aplicacao.
 *
 * <p>Telemetria ({@code contalitro/{deviceId}/telemetry}):
 * <pre>
 * {
 *   "ts": 1790000000000,        epoch em ms do ESP32; ausente, nulo ou anterior a 2020 = sem NTP
 *   "vehicle_online": true,     false = dados do veiculo congelados no ultimo valor valido
 *   "data": {
 *     "speed_kmh": 62.5, "rpm": 2100, "gear": 4, "ignition": true,
 *     "fuel_level_pct": 73.2, "fuel_flow_lph": 5.8, "instant_kml": 10.7, "eco_score": 82,
 *     "distance_km": 12.34, "fuel_used_l": 1.18, "cost_brl": 7.08,     contadores acumulados
 *     "idle_time_s": 120, "harsh_accel": 2, "harsh_brake": 1           desde que o equipamento ligou
 *   }
 * }
 * </pre>
 * Campos omitidos assumem zero (ou nulo, quando opcionais) e campos desconhecidos sao ignorados.
 * Sem o objeto {@code data}, ou com JSON malformado, a mensagem e descartada.
 *
 * <p>Status ({@code contalitro/{deviceId}/status}): {@code online} / {@code offline} em texto puro,
 * {@code {"status":"offline"}} ou {@code {"online":false}}.
 */
public class ConversorTelemetria {

    /** Antes disso o relogio do ESP32 nao foi sincronizado por NTP. */
    private static final long TS_MINIMO_MS = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();

    private final JsonMapper json = JsonMapper.shared();

    public Optional<TelemetriaRecebida> converterTelemetria(String deviceId, String payload, Instant recebidaEm) {
        JsonNode raiz;
        try {
            raiz = json.readTree(payload);
        } catch (JacksonException e) {
            return Optional.empty();
        }
        if (raiz == null || !raiz.isObject()) {
            return Optional.empty();
        }
        JsonNode data = raiz.get("data");
        if (data == null || !data.isObject()) {
            return Optional.empty();
        }

        JsonNode online = raiz.get("vehicle_online");
        boolean veiculoOnline = online == null || !online.isBoolean() || online.booleanValue();

        Contadores contadores = new Contadores(
                decimal(data, "distance_km"),
                decimal(data, "fuel_used_l"),
                BigDecimal.valueOf(decimal(data, "cost_brl")),
                decimal(data, "idle_time_s"),
                inteiro(data, "harsh_accel"),
                inteiro(data, "harsh_brake"));
        DadosTelemetria dados = new DadosTelemetria(
                decimal(data, "speed_kmh"),
                inteiro(data, "rpm"),
                inteiro(data, "gear"),
                logico(data, "ignition"),
                decimal(data, "fuel_level_pct"),
                decimal(data, "fuel_flow_lph"),
                opcional(data, "instant_kml"),
                opcional(data, "eco_score"),
                contadores);

        return Optional.of(new TelemetriaRecebida(deviceId, recebidaEm, instanteEquipamento(raiz.get("ts")),
                veiculoOnline, dados, data.toString(), payload));
    }

    /** @return vazio quando o status nao e reconhecido */
    public Optional<Boolean> converterStatus(String payload) {
        String texto = payload == null ? "" : payload.trim();
        Optional<Boolean> simples = statusEmTexto(texto);
        if (simples.isPresent()) {
            return simples;
        }
        try {
            JsonNode raiz = json.readTree(texto);
            if (raiz != null && raiz.isObject()) {
                JsonNode online = raiz.get("online");
                if (online != null && online.isBoolean()) {
                    return Optional.of(online.booleanValue());
                }
                JsonNode status = raiz.get("status");
                if (status != null && status.isString()) {
                    return statusEmTexto(status.stringValue());
                }
            }
        } catch (JacksonException e) {
            // cai no vazio abaixo
        }
        return Optional.empty();
    }

    private static Optional<Boolean> statusEmTexto(String texto) {
        return switch (texto.trim().toLowerCase(Locale.ROOT)) {
            case "online" -> Optional.of(true);
            case "offline" -> Optional.of(false);
            default -> Optional.empty();
        };
    }

    private static Instant instanteEquipamento(JsonNode ts) {
        if (ts == null || !ts.isNumber() || ts.longValue() < TS_MINIMO_MS) {
            return null;
        }
        return Instant.ofEpochMilli(ts.longValue());
    }

    private static double decimal(JsonNode data, String campo) {
        JsonNode no = data.get(campo);
        return no != null && no.isNumber() ? no.doubleValue() : 0;
    }

    private static int inteiro(JsonNode data, String campo) {
        JsonNode no = data.get(campo);
        return no != null && no.isNumber() ? (int) Math.round(no.doubleValue()) : 0;
    }

    private static boolean logico(JsonNode data, String campo) {
        JsonNode no = data.get(campo);
        return no != null && no.isBoolean() && no.booleanValue();
    }

    private static Double opcional(JsonNode data, String campo) {
        JsonNode no = data.get(campo);
        return no != null && no.isNumber() ? no.doubleValue() : null;
    }
}
