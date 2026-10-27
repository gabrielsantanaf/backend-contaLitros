package com.gabriel.backendcontalitros.infrastructure.thingsboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercita o adaptador contra um servidor HTTP local que imita o ThingsBoard. */
class ThingsBoardHttpPainelTest {

    private HttpServer servidor;
    private final AtomicReference<String> caminho = new AtomicReference<>();
    private final AtomicReference<String> corpo = new AtomicReference<>();
    private volatile int status = 200;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            caminho.set(troca.getRequestURI().getPath());
            corpo.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            troca.sendResponseHeaders(status, -1);
            troca.close();
        });
        servidor.start();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    private ThingsBoardHttpPainel painel() {
        return new ThingsBoardHttpPainel("http://127.0.0.1:" + servidor.getAddress().getPort() + "/",
                Duration.ofSeconds(2));
    }

    @Test
    void enviaTelemetriaComTokenNaUrlEDadosSemAlteracao() {
        painel().enviar("token-do-device", Instant.ofEpochMilli(1790000000000L), "{\"speed_kmh\":62.5}");

        assertThat(caminho.get()).isEqualTo("/api/v1/token-do-device/telemetry");
        assertThat(corpo.get()).isEqualTo("{\"ts\":1790000000000,\"values\":{\"speed_kmh\":62.5}}");
    }

    @Test
    void erroDoServidorNaoPropagaExcecao() {
        status = 500;

        assertThatCode(() -> painel().enviar("tok", Instant.now(), "{}")).doesNotThrowAnyException();
    }

    @Test
    void servidorForaDoArNaoPropagaExcecao() {
        ThingsBoardHttpPainel painel = painel();
        servidor.stop(0);

        assertThatCode(() -> painel.enviar("tok", Instant.now(), "{}")).doesNotThrowAnyException();
    }
}
