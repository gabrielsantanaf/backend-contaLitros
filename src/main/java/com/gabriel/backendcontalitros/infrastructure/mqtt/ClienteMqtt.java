package com.gabriel.backendcontalitros.infrastructure.mqtt;

import com.gabriel.backendcontalitros.infrastructure.config.ContaLitroProperties;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Conecta no broker e assina {@code {base}/+/telemetry} e {@code {base}/+/status}.
 *
 * <p>A mensagem sai da thread do Paho imediatamente: gravar no banco dentro do callback
 * atrasaria o keepalive e o broker derrubaria a conexao. O instante de chegada e
 * registrado aqui, antes de a mensagem entrar na fila.
 */
public class ClienteMqtt implements SmartLifecycle, MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(ClienteMqtt.class);

    static final int FASE = SmartLifecycle.DEFAULT_PHASE - 1024;

    private final ContaLitroProperties.Mqtt config;
    private final RoteadorMensagens roteador;
    private final FilaProcessamento fila;
    private final Clock relogio;
    private final ScheduledExecutorService reconexao = Executors.newSingleThreadScheduledExecutor(
            Thread.ofPlatform().name("contalitro-mqtt-conexao").daemon().factory());

    private volatile MqttClient cliente;
    private volatile boolean rodando;

    public ClienteMqtt(ContaLitroProperties.Mqtt config, RoteadorMensagens roteador,
                       FilaProcessamento fila, Clock relogio) {
        this.config = config;
        this.roteador = roteador;
        this.fila = fila;
        this.relogio = relogio;
    }

    /** So conecta com {@code contalitro.mqtt.enabled=true} e um host (MQTT_HOST) informado. */
    @Override
    public void start() {
        rodando = true;
        if (!config.configurado()) {
            log.info("Cliente MQTT desligado (defina MQTT_HOST para receber do broker)");
            return;
        }
        reconexao.execute(this::conectar);
    }

    /** Primeira conexao; depois dela o Paho reconecta sozinho (automaticReconnect). */
    private void conectar() {
        if (!rodando) {
            return;
        }
        try {
            MqttClient novo = new MqttClient(config.uri(), config.clientId(), new MemoryPersistence());
            novo.setCallback(this);
            novo.connect(opcoes());
            cliente = novo;
        } catch (MqttException e) {
            log.warn("Nao foi possivel conectar em {}: {}. Nova tentativa em {}s",
                    config.uri(), e.getMessage(), config.intervaloReconexao().toSeconds());
            reconexao.schedule(this::conectar, config.intervaloReconexao().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private MqttConnectOptions opcoes() {
        MqttConnectOptions opcoes = new MqttConnectOptions();
        opcoes.setAutomaticReconnect(true);
        opcoes.setCleanSession(true);
        opcoes.setKeepAliveInterval(30);
        opcoes.setConnectionTimeout(10);
        if (config.usuario() != null && !config.usuario().isBlank()) {
            opcoes.setUserName(config.usuario());
            opcoes.setPassword(config.senha() == null ? new char[0] : config.senha().toCharArray());
        }
        return opcoes;
    }

    /** Chamado na primeira conexao e em cada reconexao; com cleanSession e preciso reassinar. */
    @Override
    public void connectComplete(boolean reconexao, String uri) {
        String telemetria = config.topicoBase() + "/+/telemetry";
        String status = config.topicoBase() + "/+/status";
        try {
            cliente.subscribe(new String[] {telemetria, status}, new int[] {config.qos(), config.qos()});
            log.info("{} em {} | assinando {} e {}", reconexao ? "Reconectado" : "Conectado", uri, telemetria, status);
        } catch (MqttException e) {
            log.error("Falha ao assinar os topicos", e);
        }
    }

    @Override
    public void messageArrived(String topico, MqttMessage mensagem) {
        Instant recebidaEm = relogio.instant();
        String payload = new String(mensagem.getPayload(), StandardCharsets.UTF_8);
        fila.submeter(() -> roteador.rotear(topico, payload, recebidaEm));
    }

    @Override
    public void connectionLost(Throwable causa) {
        log.warn("Conexao com o broker perdida: {}. Reconectando...", causa.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // o backend nao publica
    }

    @Override
    public void stop() {
        rodando = false;
        reconexao.shutdownNow();
        MqttClient atual = cliente;
        if (atual != null) {
            try {
                if (atual.isConnected()) {
                    atual.disconnect();
                }
                atual.close();
            } catch (MqttException e) {
                log.warn("Falha ao desconectar do broker: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean isRunning() {
        return rodando;
    }

    @Override
    public int getPhase() {
        return FASE;
    }
}
