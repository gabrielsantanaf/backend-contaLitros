package com.gabriel.backendcontalitros.infrastructure.mqtt;

import com.gabriel.backendcontalitros.application.port.in.ProcessarStatusUseCase;
import com.gabriel.backendcontalitros.application.port.in.ProcessarTelemetriaUseCase;
import com.gabriel.backendcontalitros.application.port.in.ResultadoProcessamento;
import com.gabriel.backendcontalitros.application.port.in.TelemetriaRecebida;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decide, pelo topico, qual caso de uso trata a mensagem:
 * {@code {base}/{deviceId}/telemetry} ou {@code {base}/{deviceId}/status}.
 */
public class RoteadorMensagens {

    private static final Logger log = LoggerFactory.getLogger(RoteadorMensagens.class);

    private final String topicoBase;
    private final ConversorTelemetria conversor;
    private final ProcessarTelemetriaUseCase processarTelemetria;
    private final ProcessarStatusUseCase processarStatus;

    public RoteadorMensagens(String topicoBase, ConversorTelemetria conversor,
                             ProcessarTelemetriaUseCase processarTelemetria,
                             ProcessarStatusUseCase processarStatus) {
        this.topicoBase = topicoBase;
        this.conversor = conversor;
        this.processarTelemetria = processarTelemetria;
        this.processarStatus = processarStatus;
    }

    public void rotear(String topico, String payload, Instant recebidaEm) {
        String[] partes = topico.split("/");
        if (partes.length != 3 || !partes[0].equals(topicoBase) || partes[1].isBlank()) {
            log.warn("Topico ignorado: {}", topico);
            return;
        }
        String deviceId = partes[1];
        switch (partes[2]) {
            case "telemetry" -> telemetria(deviceId, payload, recebidaEm);
            case "status" -> status(deviceId, payload, recebidaEm);
            default -> log.warn("Topico ignorado: {}", topico);
        }
    }

    private void telemetria(String deviceId, String payload, Instant recebidaEm) {
        Optional<TelemetriaRecebida> telemetria = conversor.converterTelemetria(deviceId, payload, recebidaEm);
        if (telemetria.isEmpty()) {
            log.warn("Leitura descartada de {}: JSON malformado ou sem o campo data", deviceId);
            return;
        }
        ResultadoProcessamento resultado = processarTelemetria.processar(telemetria.get());
        if (resultado != ResultadoProcessamento.ACEITA) {
            log.info("Leitura descartada de {}: {}", deviceId, resultado);
        }
    }

    private void status(String deviceId, String payload, Instant recebidaEm) {
        Optional<Boolean> online = conversor.converterStatus(payload);
        if (online.isEmpty()) {
            log.warn("Status nao reconhecido de {}: {}", deviceId, payload);
            return;
        }
        boolean conhecido = processarStatus.processar(deviceId, online.get(), recebidaEm);
        log.info("Equipamento {} {}{}", deviceId, online.get() ? "online" : "offline",
                conhecido ? "" : " (nao cadastrado)");
    }
}
