package com.gabriel.backendcontalitros.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propriedades {@code contalitro.*}.
 *
 * @param autoCadastro cadastra automaticamente equipamentos desconhecidos (desligado em prod)
 * @param inatividade  tempo sem mensagens para fechar a viagem com SEM_COMUNICACAO
 */
@ConfigurationProperties("contalitro")
public record ContaLitroProperties(
        @DefaultValue("true") boolean autoCadastro,
        @DefaultValue("10m") Duration inatividade,
        @DefaultValue Mqtt mqtt,
        @DefaultValue ThingsBoard thingsboard) {

    /**
     * Conexao com o broker. O cliente so sobe com {@code enabled=true} e um host informado.
     * O backend so assina; a credencial pode ser Subscribe Only.
     */
    public record Mqtt(
            @DefaultValue("true") boolean enabled,
            String host,
            @DefaultValue("8883") int port,
            @DefaultValue("ssl") String protocolo,
            String usuario,
            String senha,
            @DefaultValue("contalitro-backend") String clientId,
            @DefaultValue("contalitro") String topicoBase,
            @DefaultValue("1") int qos,
            @DefaultValue("10s") Duration intervaloReconexao) {

        public boolean configurado() {
            return enabled && host != null && !host.isBlank();
        }

        public String uri() {
            return protocolo + "://" + host + ":" + port;
        }
    }

    public record ThingsBoard(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("https://thingsboard.cloud") String url,
            @DefaultValue("5s") Duration timeout) {
    }
}
