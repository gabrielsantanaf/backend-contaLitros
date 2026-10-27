package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.port.in.ProcessarStatusUseCase;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.time.Instant;
import java.util.Optional;

/** Trata o status do equipamento. O offline (Last Will) fecha a viagem aberta. */
public class ProcessarStatusService implements ProcessarStatusUseCase {

    private final VeiculoRepositoryPort veiculos;
    private final ViagemRepositoryPort viagens;
    private final TransacaoPort transacao;

    public ProcessarStatusService(VeiculoRepositoryPort veiculos, ViagemRepositoryPort viagens,
                                  TransacaoPort transacao) {
        this.veiculos = veiculos;
        this.viagens = viagens;
        this.transacao = transacao;
    }

    @Override
    public boolean processar(String deviceId, boolean online, Instant recebidaEm) {
        return transacao.executar(() -> {
            Optional<Veiculo> encontrado = veiculos.buscarPorDeviceId(deviceId);
            if (encontrado.isEmpty()) {
                return false;
            }
            Veiculo veiculo = encontrado.get();
            if (online) {
                veiculo.registrarComunicacao(recebidaEm);
            } else {
                veiculo.marcarOffline();
                viagens.buscarAbertaPorVeiculo(veiculo.getId()).ifPresent(viagem -> {
                    viagem.fechar(MotivoFechamento.EQUIPAMENTO_OFFLINE, recebidaEm);
                    viagens.salvar(viagem);
                });
            }
            veiculos.salvar(veiculo);
            return true;
        });
    }
}
