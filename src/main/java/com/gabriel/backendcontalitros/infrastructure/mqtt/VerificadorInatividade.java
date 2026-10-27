package com.gabriel.backendcontalitros.infrastructure.mqtt;

import com.gabriel.backendcontalitros.application.port.in.FecharViagensInativasUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tarefa agendada que fecha as viagens sem comunicacao. Roda na mesma fila das
 * mensagens para nunca disputar uma viagem com o processamento de uma leitura.
 */
public class VerificadorInatividade {

    private static final Logger log = LoggerFactory.getLogger(VerificadorInatividade.class);

    private final FecharViagensInativasUseCase fecharViagensInativas;
    private final FilaProcessamento fila;

    public VerificadorInatividade(FecharViagensInativasUseCase fecharViagensInativas, FilaProcessamento fila) {
        this.fecharViagensInativas = fecharViagensInativas;
        this.fila = fila;
    }

    @Scheduled(fixedDelayString = "${contalitro.verificacao-inatividade:60s}")
    public void verificar() {
        fila.submeter(() -> {
            int fechadas = fecharViagensInativas.fecharInativas();
            if (fechadas > 0) {
                log.info("{} viagem(ns) fechada(s) por falta de comunicacao", fechadas);
            }
        });
    }
}
