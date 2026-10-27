package com.gabriel.backendcontalitros.infrastructure.thingsboard;

import com.gabriel.backendcontalitros.application.port.out.PainelPort;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Envia a telemetria pela API HTTP de dispositivos do ThingsBoard:
 * {@code POST {url}/api/v1/{token}/telemetry}. O token vai em cada requisicao,
 * entao um unico cliente atende a frota inteira (por MQTT seria uma conexao por veiculo).
 *
 * <p>Falhas sao registradas no log e nunca propagadas: o que ja foi gravado no banco
 * nao depende do painel. O token e segredo e nao aparece no log.
 */
public class ThingsBoardHttpPainel implements PainelPort {

    private static final Logger log = LoggerFactory.getLogger(ThingsBoardHttpPainel.class);

    private final HttpClient http;
    private final String url;
    private final Duration timeout;

    public ThingsBoardHttpPainel(String url, Duration timeout) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.timeout = timeout;
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public void enviar(String token, Instant instante, String dadosJson) {
        String corpo = "{\"ts\":" + instante.toEpochMilli() + ",\"values\":" + dadosJson + "}";
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(
                        url + "/api/v1/" + URLEncoder.encode(token, StandardCharsets.UTF_8) + "/telemetry"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        try {
            HttpResponse<Void> resposta = http.send(requisicao, HttpResponse.BodyHandlers.discarding());
            if (resposta.statusCode() / 100 != 2) {
                log.warn("ThingsBoard recusou a telemetria: HTTP {}", resposta.statusCode());
            }
        } catch (IOException e) {
            log.warn("Falha ao enviar telemetria ao ThingsBoard: {}", e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Envio ao ThingsBoard interrompido");
        }
    }
}
