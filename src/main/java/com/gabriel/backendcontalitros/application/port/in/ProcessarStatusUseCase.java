package com.gabriel.backendcontalitros.application.port.in;

import java.time.Instant;

public interface ProcessarStatusUseCase {

    /**
     * Registra o status publicado pelo equipamento (online ou o Last Will offline).
     *
     * @return false se o equipamento nao esta cadastrado
     */
    boolean processar(String deviceId, boolean online, Instant recebidaEm);
}
