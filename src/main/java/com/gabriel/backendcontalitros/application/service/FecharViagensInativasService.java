package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.port.in.FecharViagensInativasUseCase;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Fecha com SEM_COMUNICACAO as viagens que ficaram sem leitura alem do tempo
 * limite. O fim da viagem e o instante da ultima leitura recebida.
 */
public class FecharViagensInativasService implements FecharViagensInativasUseCase {

    private final ViagemRepositoryPort viagens;
    private final TransacaoPort transacao;
    private final Clock relogio;
    private final Duration tempoLimite;

    public FecharViagensInativasService(ViagemRepositoryPort viagens, TransacaoPort transacao,
                                        Clock relogio, Duration tempoLimite) {
        this.viagens = viagens;
        this.transacao = transacao;
        this.relogio = relogio;
        this.tempoLimite = tempoLimite;
    }

    @Override
    public int fecharInativas() {
        Instant limite = relogio.instant().minus(tempoLimite);
        return transacao.executar(() -> {
            List<Viagem> inativas = viagens.listarAbertasSemLeituraDesde(limite);
            for (Viagem viagem : inativas) {
                viagem.fechar(MotivoFechamento.SEM_COMUNICACAO, viagem.getUltimaLeituraEm());
                viagens.salvar(viagem);
            }
            return inativas.size();
        });
    }
}
