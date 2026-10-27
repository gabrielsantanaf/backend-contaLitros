package com.gabriel.backendcontalitros.infrastructure.config;

import com.gabriel.backendcontalitros.application.port.in.FecharViagensInativasUseCase;
import com.gabriel.backendcontalitros.application.port.in.ProcessarStatusUseCase;
import com.gabriel.backendcontalitros.application.port.in.ProcessarTelemetriaUseCase;
import com.gabriel.backendcontalitros.infrastructure.mqtt.ClienteMqtt;
import com.gabriel.backendcontalitros.infrastructure.mqtt.ConversorTelemetria;
import com.gabriel.backendcontalitros.infrastructure.mqtt.FilaProcessamento;
import com.gabriel.backendcontalitros.infrastructure.mqtt.RoteadorMensagens;
import com.gabriel.backendcontalitros.infrastructure.mqtt.VerificadorInatividade;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entrada pelo broker MQTT e a verificacao periodica de inatividade. */
@Configuration
@EnableScheduling
public class MqttConfig {

    @Bean
    FilaProcessamento filaProcessamento() {
        return new FilaProcessamento();
    }

    @Bean
    RoteadorMensagens roteadorMensagens(ContaLitroProperties props, ProcessarTelemetriaUseCase processarTelemetria,
                                        ProcessarStatusUseCase processarStatus) {
        return new RoteadorMensagens(props.mqtt().topicoBase(), new ConversorTelemetria(),
                processarTelemetria, processarStatus);
    }

    @Bean
    VerificadorInatividade verificadorInatividade(FecharViagensInativasUseCase fecharViagensInativas,
                                                  FilaProcessamento fila) {
        return new VerificadorInatividade(fecharViagensInativas, fila);
    }

    @Bean
    ClienteMqtt clienteMqtt(ContaLitroProperties props, RoteadorMensagens roteador,
                            FilaProcessamento fila, Clock relogio) {
        return new ClienteMqtt(props.mqtt(), roteador, fila, relogio);
    }
}
