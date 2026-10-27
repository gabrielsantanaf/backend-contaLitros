package com.gabriel.backendcontalitros.infrastructure.persistence.entity;

import com.gabriel.backendcontalitros.domain.model.Veiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "veiculo")
public class VeiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String deviceId;

    @Column(length = 10)
    private String placa;

    @Column(length = 80)
    private String modelo;

    @Column(name = "capacidade_tanque_l", nullable = false)
    private double capacidadeTanqueL;

    private Double metaConsumoKml;

    @Column(length = 120)
    private String thingsboardToken;

    @Column(nullable = false)
    private boolean equipamentoOnline;

    private Instant ultimaComunicacao;

    @Column(nullable = false)
    private Instant criadoEm;

    protected VeiculoEntity() {
    }

    public static VeiculoEntity de(Veiculo v) {
        VeiculoEntity e = new VeiculoEntity();
        e.id = v.getId();
        e.deviceId = v.getDeviceId();
        e.placa = v.getPlaca();
        e.modelo = v.getModelo();
        e.capacidadeTanqueL = v.getCapacidadeTanqueL();
        e.metaConsumoKml = v.getMetaConsumoKml();
        e.thingsboardToken = v.getThingsboardToken();
        e.equipamentoOnline = v.isEquipamentoOnline();
        e.ultimaComunicacao = v.getUltimaComunicacao();
        e.criadoEm = v.getCriadoEm();
        return e;
    }

    public Veiculo paraDominio() {
        return new Veiculo(id, deviceId, placa, modelo, capacidadeTanqueL, metaConsumoKml,
                thingsboardToken, equipamentoOnline, ultimaComunicacao, criadoEm);
    }
}
