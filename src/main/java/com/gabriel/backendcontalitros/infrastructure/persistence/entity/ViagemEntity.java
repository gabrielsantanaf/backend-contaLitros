package com.gabriel.backendcontalitros.infrastructure.persistence.entity;

import com.gabriel.backendcontalitros.domain.model.Contadores;
import com.gabriel.backendcontalitros.domain.model.MotivoFechamento;
import com.gabriel.backendcontalitros.domain.model.StatusViagem;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "viagem")
public class ViagemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusViagem status;

    @Column(nullable = false)
    private Instant inicio;

    private Instant fim;

    @Column(nullable = false)
    private Instant ultimaLeituraEm;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotivoFechamento motivoFechamento;

    // valores da viagem (ja descontada a base)
    @Column(nullable = false)
    private double distanciaKm;
    @Column(name = "combustivel_l", nullable = false)
    private double combustivelL;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custoBrl;
    @Column(name = "tempo_ocioso_s", nullable = false)
    private double tempoOciosoS;
    @Column(nullable = false)
    private int aceleracoesBruscas;
    @Column(nullable = false)
    private int frenagensBruscas;
    private Double notaEficiencia;

    // valores absolutos do equipamento no momento em que a viagem abriu
    @Column(nullable = false)
    private double baseDistanciaKm;
    @Column(name = "base_combustivel_l", nullable = false)
    private double baseCombustivelL;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseCustoBrl;
    @Column(name = "base_tempo_ocioso_s", nullable = false)
    private double baseTempoOciosoS;
    @Column(nullable = false)
    private int baseAceleracoes;
    @Column(nullable = false)
    private int baseFrenagens;

    protected ViagemEntity() {
    }

    public static ViagemEntity de(Viagem v) {
        ViagemEntity e = new ViagemEntity();
        e.id = v.getId();
        e.veiculoId = v.getVeiculoId();
        e.status = v.getStatus();
        e.inicio = v.getInicio();
        e.fim = v.getFim();
        e.ultimaLeituraEm = v.getUltimaLeituraEm();
        e.motivoFechamento = v.getMotivoFechamento();
        e.notaEficiencia = v.getNotaEficiencia();

        Contadores totais = v.getTotais();
        e.distanciaKm = totais.distanciaKm();
        e.combustivelL = totais.combustivelL();
        e.custoBrl = totais.custoBrl();
        e.tempoOciosoS = totais.tempoOciosoS();
        e.aceleracoesBruscas = totais.aceleracoesBruscas();
        e.frenagensBruscas = totais.frenagensBruscas();

        Contadores base = v.getBase();
        e.baseDistanciaKm = base.distanciaKm();
        e.baseCombustivelL = base.combustivelL();
        e.baseCustoBrl = base.custoBrl();
        e.baseTempoOciosoS = base.tempoOciosoS();
        e.baseAceleracoes = base.aceleracoesBruscas();
        e.baseFrenagens = base.frenagensBruscas();
        return e;
    }

    public Viagem paraDominio() {
        Contadores totais = new Contadores(distanciaKm, combustivelL, custoBrl, tempoOciosoS,
                aceleracoesBruscas, frenagensBruscas);
        Contadores base = new Contadores(baseDistanciaKm, baseCombustivelL, baseCustoBrl, baseTempoOciosoS,
                baseAceleracoes, baseFrenagens);
        return new Viagem(id, veiculoId, status, inicio, fim, ultimaLeituraEm, motivoFechamento,
                totais, notaEficiencia, base);
    }
}
