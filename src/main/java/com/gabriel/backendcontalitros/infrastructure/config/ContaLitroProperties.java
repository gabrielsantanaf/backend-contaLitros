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
        @DefaultValue ThingsBoard thingsboard) {

    public record ThingsBoard(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("https://thingsboard.cloud") String url,
            @DefaultValue("5s") Duration timeout) {
    }
}
