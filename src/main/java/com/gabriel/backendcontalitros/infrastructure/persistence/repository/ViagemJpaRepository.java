package com.gabriel.backendcontalitros.infrastructure.persistence.repository;

import com.gabriel.backendcontalitros.domain.model.StatusViagem;
import com.gabriel.backendcontalitros.infrastructure.persistence.entity.ViagemEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ViagemJpaRepository extends JpaRepository<ViagemEntity, Long> {

    Optional<ViagemEntity> findFirstByVeiculoIdAndStatusOrderByInicioDesc(Long veiculoId, StatusViagem status);

    List<ViagemEntity> findByVeiculoIdOrderByInicioDesc(Long veiculoId);

    List<ViagemEntity> findByStatusAndUltimaLeituraEmBefore(StatusViagem status, Instant limite);

    @Query("select v from ViagemEntity v where v.inicio >= :de and v.inicio < :ate")
    List<ViagemEntity> findIniciadasEntre(Instant de, Instant ate);
}
