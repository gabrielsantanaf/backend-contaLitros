package com.gabriel.backendcontalitros.infrastructure.thingsboard;

import com.gabriel.backendcontalitros.application.port.out.PainelPort;
import java.time.Instant;

/** Usado quando TB_ENABLED nao esta ligado: os dados ficam so no banco. */
public class PainelDesabilitado implements PainelPort {

    @Override
    public void enviar(String token, Instant instante, String dadosJson) {
        // envio ao painel desligado
    }
}
