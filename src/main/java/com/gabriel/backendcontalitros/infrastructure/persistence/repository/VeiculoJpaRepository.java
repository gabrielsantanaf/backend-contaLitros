package com.gabriel.backendcontalitros.infrastructure.persistence.repository;

import com.gabriel.backendcontalitros.infrastructure.persistence.entity.VeiculoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, Long> {

    Optional<VeiculoEntity> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
