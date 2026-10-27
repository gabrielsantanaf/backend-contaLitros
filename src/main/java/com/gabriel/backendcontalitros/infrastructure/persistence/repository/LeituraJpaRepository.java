package com.gabriel.backendcontalitros.infrastructure.persistence.repository;

import com.gabriel.backendcontalitros.infrastructure.persistence.entity.LeituraEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeituraJpaRepository extends JpaRepository<LeituraEntity, Long> {

    boolean existsByVeiculoIdAndInstanteEquipamento(Long veiculoId, Instant instanteEquipamento);

    @Query("""
            select l from LeituraEntity l
            where l.veiculoId = :veiculoId and l.recebidaEm >= :de and l.recebidaEm < :ate
            order by l.recebidaEm
            """)
    List<LeituraEntity> findPorVeiculoNoPeriodo(Long veiculoId, Instant de, Instant ate);
}
