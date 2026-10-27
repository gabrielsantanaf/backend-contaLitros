package com.gabriel.backendcontalitros.infrastructure.config;

import com.gabriel.backendcontalitros.application.port.in.CadastrarVeiculoUseCase;
import com.gabriel.backendcontalitros.application.port.in.ConsultarLeiturasUseCase;
import com.gabriel.backendcontalitros.application.port.in.ConsultarViagensUseCase;
import com.gabriel.backendcontalitros.application.port.in.FecharViagensInativasUseCase;
import com.gabriel.backendcontalitros.application.port.in.GerarRelatorioConsumoUseCase;
import com.gabriel.backendcontalitros.application.port.in.ListarVeiculosUseCase;
import com.gabriel.backendcontalitros.application.port.in.ProcessarStatusUseCase;
import com.gabriel.backendcontalitros.application.port.in.ProcessarTelemetriaUseCase;
import com.gabriel.backendcontalitros.application.port.out.LeituraRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.PainelPort;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.application.service.CadastrarVeiculoService;
import com.gabriel.backendcontalitros.application.service.ConsultarLeiturasService;
import com.gabriel.backendcontalitros.application.service.ConsultarViagensService;
import com.gabriel.backendcontalitros.application.service.FecharViagensInativasService;
import com.gabriel.backendcontalitros.application.service.GerarRelatorioConsumoService;
import com.gabriel.backendcontalitros.application.service.ListarVeiculosService;
import com.gabriel.backendcontalitros.application.service.ProcessarStatusService;
import com.gabriel.backendcontalitros.application.service.ProcessarTelemetriaService;
import com.gabriel.backendcontalitros.infrastructure.thingsboard.PainelDesabilitado;
import com.gabriel.backendcontalitros.infrastructure.thingsboard.ThingsBoardHttpPainel;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Liga os casos de uso (Java puro) aos adaptadores. E o unico lugar onde a
 * camada de aplicacao encontra o Spring.
 */
@Configuration
@EnableConfigurationProperties(ContaLitroProperties.class)
public class CasosDeUsoConfig {

    private static final Logger log = LoggerFactory.getLogger(CasosDeUsoConfig.class);

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    PainelPort painel(ContaLitroProperties props) {
        ContaLitroProperties.ThingsBoard tb = props.thingsboard();
        if (!tb.enabled()) {
            log.info("Envio ao ThingsBoard desligado (TB_ENABLED=false)");
            return new PainelDesabilitado();
        }
        log.info("Envio ao ThingsBoard ligado: {}", tb.url());
        return new ThingsBoardHttpPainel(tb.url(), tb.timeout());
    }

    @Bean
    ProcessarTelemetriaUseCase processarTelemetria(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens,
                                                   LeituraRepositoryPort leituras, PainelPort painel,
                                                   TransacaoPort transacao, ContaLitroProperties props) {
        return new ProcessarTelemetriaService(veiculos, viagens, leituras, painel, transacao, props.autoCadastro());
    }

    @Bean
    ProcessarStatusUseCase processarStatus(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens,
                                           TransacaoPort transacao) {
        return new ProcessarStatusService(veiculos, viagens, transacao);
    }

    @Bean
    FecharViagensInativasUseCase fecharViagensInativas(ViagemRepositoryPort viagens, TransacaoPort transacao,
                                                       Clock relogio, ContaLitroProperties props) {
        return new FecharViagensInativasService(viagens, transacao, relogio, props.inatividade());
    }

    @Bean
    CadastrarVeiculoUseCase cadastrarVeiculo(VeiculoRepositoryPort veiculos, TransacaoPort transacao, Clock relogio) {
        return new CadastrarVeiculoService(veiculos, transacao, relogio);
    }

    @Bean
    ListarVeiculosUseCase listarVeiculos(VeiculoRepositoryPort veiculos) {
        return new ListarVeiculosService(veiculos);
    }

    @Bean
    ConsultarViagensUseCase consultarViagens(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens) {
        return new ConsultarViagensService(veiculos, viagens);
    }

    @Bean
    ConsultarLeiturasUseCase consultarLeituras(VeiculoRepositoryPort veiculos, LeituraRepositoryPort leituras,
                                               Clock relogio) {
        return new ConsultarLeiturasService(veiculos, leituras, relogio);
    }

    @Bean
    GerarRelatorioConsumoUseCase gerarRelatorioConsumo(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens,
                                                       Clock relogio) {
        return new GerarRelatorioConsumoService(veiculos, viagens, relogio);
    }
}
