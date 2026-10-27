package com.gabriel.backendcontalitros.infrastructure.persistence;

import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.infrastructure.persistence.entity.VeiculoEntity;
import com.gabriel.backendcontalitros.infrastructure.persistence.repository.VeiculoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class VeiculoPersistenceAdapter implements VeiculoRepositoryPort {

    private final VeiculoJpaRepository repository;

    public VeiculoPersistenceAdapter(VeiculoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return repository.save(VeiculoEntity.de(veiculo)).paraDominio();
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return repository.findById(id).map(VeiculoEntity::paraDominio);
    }

    @Override
    public Optional<Veiculo> buscarPorDeviceId(String deviceId) {
        return repository.findByDeviceId(deviceId).map(VeiculoEntity::paraDominio);
    }

    @Override
    public boolean existePorDeviceId(String deviceId) {
        return repository.existsByDeviceId(deviceId);
    }

    @Override
    public List<Veiculo> listarTodos() {
        return repository.findAll(Sort.by("id")).stream().map(VeiculoEntity::paraDominio).toList();
    }
}
