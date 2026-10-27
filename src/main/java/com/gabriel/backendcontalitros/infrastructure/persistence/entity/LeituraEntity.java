package com.gabriel.backendcontalitros.infrastructure.persistence.entity;

import com.gabriel.backendcontalitros.domain.model.Leitura;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "leitura")
public class LeituraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long veiculoId;

    private Long viagemId;

    @Column(nullable = false)
    private Instant recebidaEm;

    private Instant instanteEquipamento;

    @Column(nullable = false)
    private double velocidadeKmh;
    @Column(nullable = false)
    private int rpm;
    @Column(nullable = false)
    private int marcha;
    @Column(nullable = false)
    private double tanquePct;
    @Column(nullable = false)
    private double vazaoLph;
    private Double consumoKml;
    @Column(nullable = false)
    private boolean ignicao;

    // TEXT, e nao @Lob: no PostgreSQL @Lob em String vira large object, fora da tabela
    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    protected LeituraEntity() {
    }

    public static LeituraEntity de(Leitura l) {
        LeituraEntity e = new LeituraEntity();
        e.id = l.id();
        e.veiculoId = l.veiculoId();
        e.viagemId = l.viagemId();
        e.recebidaEm = l.recebidaEm();
        e.instanteEquipamento = l.instanteEquipamento();
        e.velocidadeKmh = l.velocidadeKmh();
        e.rpm = l.rpm();
        e.marcha = l.marcha();
        e.tanquePct = l.tanquePct();
        e.vazaoLph = l.vazaoLph();
        e.consumoKml = l.consumoKml();
        e.ignicao = l.ignicao();
        e.payload = l.payload();
        return e;
    }

    public Leitura paraDominio() {
        return new Leitura(id, veiculoId, viagemId, recebidaEm, instanteEquipamento, velocidadeKmh,
                rpm, marcha, tanquePct, vazaoLph, consumoKml, ignicao, payload);
    }
}
