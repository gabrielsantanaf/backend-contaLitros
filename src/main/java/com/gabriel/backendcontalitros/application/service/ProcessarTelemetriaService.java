package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.port.in.ProcessarTelemetriaUseCase;
import com.gabriel.backendcontalitros.application.port.in.ResultadoProcessamento;
import com.gabriel.backendcontalitros.application.port.in.TelemetriaRecebida;
import com.gabriel.backendcontalitros.application.port.out.LeituraRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.PainelPort;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Leitura;
import com.gabriel.backendcontalitros.domain.model.RegrasDeViagem;
import com.gabriel.backendcontalitros.domain.model.RegrasDeViagem.Transicao;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.util.Optional;

/**
 * Registra uma leitura de telemetria: valida, aplica as regras de viagem e grava
 * tudo em uma transacao. O envio ao painel acontece depois, fora da transacao:
 * uma requisicao lenta nao segura o banco e uma falha no painel nao desfaz o
 * que ja foi gravado.
 */
public class ProcessarTelemetriaService implements ProcessarTelemetriaUseCase {

    private final VeiculoRepositoryPort veiculos;
    private final ViagemRepositoryPort viagens;
    private final LeituraRepositoryPort leituras;
    private final PainelPort painel;
    private final TransacaoPort transacao;
    private final boolean autoCadastro;

    public ProcessarTelemetriaService(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens,
                                      LeituraRepositoryPort leituras, PainelPort painel,
                                      TransacaoPort transacao, boolean autoCadastro) {
        this.veiculos = veiculos;
        this.viagens = viagens;
        this.leituras = leituras;
        this.painel = painel;
        this.transacao = transacao;
        this.autoCadastro = autoCadastro;
    }

    @Override
    public ResultadoProcessamento processar(TelemetriaRecebida telemetria) {
        if (!telemetria.veiculoOnline()) {
            return ResultadoProcessamento.DESCARTADA_VEICULO_OFFLINE;
        }

        Registro registro = transacao.executar(() -> registrar(telemetria));

        if (registro.resultado() == ResultadoProcessamento.ACEITA && registro.veiculo().possuiTokenPainel()) {
            painel.enviar(registro.veiculo().getThingsboardToken(),
                    telemetria.instanteReferencia(), telemetria.dadosJson());
        }
        return registro.resultado();
    }

    private Registro registrar(TelemetriaRecebida telemetria) {
        Optional<Veiculo> cadastrado = veiculos.buscarPorDeviceId(telemetria.deviceId());
        if (cadastrado.isEmpty() && !autoCadastro) {
            return Registro.descartada(ResultadoProcessamento.DESCARTADA_VEICULO_DESCONHECIDO);
        }
        Veiculo veiculo = cadastrado.orElseGet(() ->
                veiculos.salvar(Veiculo.autoCadastrado(telemetria.deviceId(), telemetria.recebidaEm())));

        if (telemetria.instanteEquipamento() != null
                && leituras.existe(veiculo.getId(), telemetria.instanteEquipamento())) {
            return Registro.descartada(ResultadoProcessamento.DESCARTADA_DUPLICADA);
        }

        veiculo.registrarComunicacao(telemetria.recebidaEm());
        veiculo = veiculos.salvar(veiculo);

        Viagem aberta = viagens.buscarAbertaPorVeiculo(veiculo.getId()).orElse(null);
        Transicao transicao = RegrasDeViagem.aplicar(
                veiculo.getId(), aberta, telemetria.dados(), telemetria.recebidaEm());

        Viagem fechada = transicao.fechada() != null ? viagens.salvar(transicao.fechada()) : null;
        Viagem atual = transicao.aberta() != null ? viagens.salvar(transicao.aberta()) : null;
        Long viagemId = switch (transicao.vinculo()) {
            case VIAGEM_FECHADA -> fechada.getId();
            case VIAGEM_ABERTA -> atual.getId();
            case NENHUM -> null;
        };

        leituras.salvar(Leitura.registrar(veiculo.getId(), viagemId, telemetria.recebidaEm(),
                telemetria.instanteEquipamento(), telemetria.dados(), telemetria.payload()));

        return new Registro(ResultadoProcessamento.ACEITA, veiculo);
    }

    private record Registro(ResultadoProcessamento resultado, Veiculo veiculo) {

        static Registro descartada(ResultadoProcessamento motivo) {
            return new Registro(motivo, null);
        }
    }
}
