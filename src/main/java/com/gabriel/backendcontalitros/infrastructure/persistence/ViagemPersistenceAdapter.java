package com.gabriel.backendcontalitros.infrastructure.persistence;

import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.StatusViagem;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import com.gabriel.backendcontalitros.infrastructure.persistence.entity.ViagemEntity;
import com.gabriel.backendcontalitros.infrastructure.persistence.repository.ViagemJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ViagemPersistenceAdapter implements ViagemRepositoryPort {

    private final ViagemJpaRepository repository;

    public ViagemPersistenceAdapter(ViagemJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Viagem salvar(Viagem viagem) {
        return repository.save(ViagemEntity.de(viagem)).paraDominio();
    }

    @Override
    public Optional<Viagem> buscarPorId(Long id) {
        return repository.findById(id).map(ViagemEntity::paraDominio);
    }

    @Override
    public Optional<Viagem> buscarAbertaPorVeiculo(Long veiculoId) {
        return repository.findFirstByVeiculoIdAndStatusOrderByInicioDesc(veiculoId, StatusViagem.ABERTA)
                .map(ViagemEntity::paraDominio);
    }

    @Override
    public List<Viagem> listarPorVeiculo(Long veiculoId) {
        return repository.findByVeiculoIdOrderByInicioDesc(veiculoId).stream()
                .map(ViagemEntity::paraDominio).toList();
    }

    @Override
    public List<Viagem> listarAbertasSemLeituraDesde(Instant limite) {
        return repository.findByStatusAndUltimaLeituraEmBefore(StatusViagem.ABERTA, limite).stream()
                .map(ViagemEntity::paraDominio).toList();
    }

    @Override
    public List<Viagem> listarIniciadasEntre(Instant de, Instant ate) {
        return repository.findIniciadasEntre(de, ate).stream().map(ViagemEntity::paraDominio).toList();
    }
}
