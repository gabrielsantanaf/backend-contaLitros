package com.gabriel.backendcontalitros.application.port.in;

public interface ProcessarTelemetriaUseCase {

    ResultadoProcessamento processar(TelemetriaRecebida telemetria);
}
