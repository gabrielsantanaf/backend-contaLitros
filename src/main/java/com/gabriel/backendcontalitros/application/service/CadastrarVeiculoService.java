package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.exception.DeviceIdJaCadastradoException;
import com.gabriel.backendcontalitros.application.port.in.CadastrarVeiculoUseCase;
import com.gabriel.backendcontalitros.application.port.in.NovoVeiculo;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import java.time.Clock;

public class CadastrarVeiculoService implements CadastrarVeiculoUseCase {

    private final VeiculoRepositoryPort veiculos;
    private final TransacaoPort transacao;
    private final Clock relogio;

    public CadastrarVeiculoService(VeiculoRepositoryPort veiculos, TransacaoPort transacao, Clock relogio) {
        this.veiculos = veiculos;
        this.transacao = transacao;
        this.relogio = relogio;
    }

    @Override
    public Veiculo cadastrar(NovoVeiculo novo) {
        return transacao.executar(() -> {
            if (veiculos.existePorDeviceId(novo.deviceId())) {
                throw new DeviceIdJaCadastradoException(novo.deviceId());
            }
            return veiculos.salvar(Veiculo.novo(novo.deviceId(), novo.placa(), novo.modelo(),
                    novo.capacidadeTanqueL(), novo.metaConsumoKml(), novo.thingsboardToken(),
                    relogio.instant()));
        });
    }
}
