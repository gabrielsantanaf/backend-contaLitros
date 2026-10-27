package com.gabriel.backendcontalitros.infrastructure.mqtt;

import static org.assertj.core.api.Assertions.assertThat;

import com.gabriel.backendcontalitros.application.port.in.TelemetriaRecebida;
import com.gabriel.backendcontalitros.domain.model.DadosTelemetria;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Testes de contrato: o backend le exatamente o formato publicado pelo Conta Litro. */
class ConversorTelemetriaTest {

    private static final Instant RECEBIDA = Instant.parse("2026-10-01T12:00:05Z");

    private static final String COMPLETA = """
            {
              "ts": 1790000000000,
              "vehicle_online": true,
              "data": {
                "speed_kmh": 62.5, "rpm": 2100, "gear": 4, "ignition": true,
                "fuel_level_pct": 73.2, "fuel_flow_lph": 5.8, "instant_kml": 10.7, "eco_score": 82,
                "distance_km": 12.34, "fuel_used_l": 1.18, "cost_brl": 7.084,
                "idle_time_s": 120, "harsh_accel": 2, "harsh_brake": 1
              }
            }
            """;

    private final ConversorTelemetria conversor = new ConversorTelemetria();

    @Test
    void leMensagemCompleta() {
        TelemetriaRecebida t = conversor.converterTelemetria("contalitro-001", COMPLETA, RECEBIDA).orElseThrow();
        DadosTelemetria d = t.dados();

        assertThat(t.deviceId()).isEqualTo("contalitro-001");
        assertThat(t.instanteEquipamento()).isEqualTo(Instant.ofEpochMilli(1790000000000L));
        assertThat(t.recebidaEm()).isEqualTo(RECEBIDA);
        assertThat(t.veiculoOnline()).isTrue();
        assertThat(d.velocidadeKmh()).isEqualTo(62.5);
        assertThat(d.rpm()).isEqualTo(2100);
        assertThat(d.marcha()).isEqualTo(4);
        assertThat(d.ignicao()).isTrue();
        assertThat(d.tanquePct()).isEqualTo(73.2);
        assertThat(d.vazaoLph()).isEqualTo(5.8);
        assertThat(d.consumoKml()).isEqualTo(10.7);
        assertThat(d.notaEficiencia()).isEqualTo(82.0);
        assertThat(d.contadores().distanciaKm()).isEqualTo(12.34);
        assertThat(d.contadores().combustivelL()).isEqualTo(1.18);
        assertThat(d.contadores().custoBrl()).isEqualByComparingTo("7.08");
        assertThat(d.contadores().tempoOciosoS()).isEqualTo(120);
        assertThat(d.contadores().aceleracoesBruscas()).isEqualTo(2);
        assertThat(d.contadores().frenagensBruscas()).isEqualTo(1);
        assertThat(t.payload()).isEqualTo(COMPLETA);
        assertThat(t.dadosJson()).startsWith("{\"speed_kmh\":62.5");
    }

    @Test
    void camposOmitidosAssumemZeroOuNuloECamposDesconhecidosSaoIgnorados() {
        String payload = """
                {"data": {"speed_kmh": 0, "ignition": false, "firmware": "1.4.2", "extra": {"x": 1}}}
                """;

        TelemetriaRecebida t = conversor.converterTelemetria("contalitro-001", payload, RECEBIDA).orElseThrow();

        assertThat(t.instanteEquipamento()).isNull();
        assertThat(t.veiculoOnline()).isTrue();
        assertThat(t.dados().ignicao()).isFalse();
        assertThat(t.dados().rpm()).isZero();
        assertThat(t.dados().consumoKml()).isNull();
        assertThat(t.dados().contadores().distanciaKm()).isZero();
    }

    @Test
    void vehicleOnlineFalseEPreservadoParaOCasoDeUsoDescartar() {
        String payload = "{\"vehicle_online\": false, \"data\": {\"ignition\": true}}";

        assertThat(conversor.converterTelemetria("d", payload, RECEBIDA).orElseThrow().veiculoOnline()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 12345, 1_500_000_000_000L})
    void tsDeRelogioNaoSincronizadoViraNulo(long ts) {
        String payload = "{\"ts\": " + ts + ", \"data\": {}}";

        assertThat(conversor.converterTelemetria("d", payload, RECEBIDA).orElseThrow().instanteEquipamento()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "nao e json", "{\"ts\": 1}", "{\"data\": 5}", "[1,2]", "{\"data\": {"})
    void descartaJsonMalformadoOuSemData(String payload) {
        assertThat(conversor.converterTelemetria("d", payload, RECEBIDA)).isEmpty();
    }

    @Test
    void reconheceOsFormatosDeStatus() {
        assertThat(conversor.converterStatus("online")).contains(true);
        assertThat(conversor.converterStatus(" OFFLINE ")).contains(false);
        assertThat(conversor.converterStatus("{\"status\": \"offline\"}")).contains(false);
        assertThat(conversor.converterStatus("{\"online\": true}")).contains(true);
        assertThat(conversor.converterStatus("talvez")).isEmpty();
        assertThat(conversor.converterStatus("{\"status\": 1}")).isEmpty();
    }
}
