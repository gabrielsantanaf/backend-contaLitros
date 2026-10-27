package com.gabriel.backendcontalitros.application.port.out;

import java.time.Instant;

/** Saida para o painel de visualizacao (ThingsBoard). Chamada fora da transacao do banco. */
public interface PainelPort {

    /**
     * @param token     token do dispositivo do veiculo no painel
     * @param instante  instante da leitura
     * @param dadosJson campo data da mensagem original, repassado sem alteracao
     */
    void enviar(String token, Instant instante, String dadosJson);
}
