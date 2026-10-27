package com.gabriel.backendcontalitros.infrastructure.persistence;

import com.gabriel.backendcontalitros.application.port.out.LeituraRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Leitura;
import com.gabriel.backendcontalitros.infrastructure.persistence.entity.LeituraEntity;
import com.gabriel.backendcontalitros.infrastructure.persistence.repository.LeituraJpaRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LeituraPersistenceAdapter implements LeituraRepositoryPort {

    private final LeituraJpaRepository repository;

    public LeituraPersistenceAdapter(LeituraJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Leitura salvar(Leitura leitura) {
        return repository.save(LeituraEntity.de(leitura)).paraDominio();
    }

    @Override
    public boolean existe(Long veiculoId, Instant instanteEquipamento) {
        return repository.existsByVeiculoIdAndInstanteEquipamento(veiculoId, instanteEquipamento);
    }

    @Override
    public List<Leitura> listarPorVeiculo(Long veiculoId, Instant de, Instant ate) {
        return repository.findPorVeiculoNoPeriodo(veiculoId, de, ate).stream()
                .map(LeituraEntity::paraDominio).toList();
    }
}
