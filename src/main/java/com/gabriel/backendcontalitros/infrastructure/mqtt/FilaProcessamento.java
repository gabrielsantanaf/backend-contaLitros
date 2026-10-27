package com.gabriel.backendcontalitros.infrastructure.mqtt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Fila com UMA unica thread. Tudo que altera viagens passa por aqui (mensagens do
 * broker e a verificacao de inatividade), entao a ordem de chegada e respeitada e
 * duas tarefas nunca mexem na mesma viagem ao mesmo tempo.
 *
 * <p>No desligamento, para depois do {@link ClienteMqtt} e antes de o banco ser
 * fechado, terminando as mensagens que ja estavam na fila.
 */
public class FilaProcessamento implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(FilaProcessamento.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("contalitro-processamento").factory());
    private volatile boolean rodando;

    public void submeter(Runnable tarefa) {
        executor.execute(() -> {
            try {
                tarefa.run();
            } catch (RuntimeException e) {
                log.error("Falha ao processar mensagem", e);
            }
        });
    }

    @Override
    public void start() {
        rodando = true;
    }

    @Override
    public void stop() {
        rodando = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return rodando;
    }

    /** Fases menores param depois: a fila para depois do cliente MQTT. */
    @Override
    public int getPhase() {
        return ClienteMqtt.FASE - 1;
    }
}
