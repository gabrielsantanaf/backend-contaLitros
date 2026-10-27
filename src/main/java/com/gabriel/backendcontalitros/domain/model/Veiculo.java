package com.gabriel.backendcontalitros.domain.model;

import java.time.Instant;
import java.util.Objects;

/** Um veiculo e um equipamento Conta Litro instalado nele. */
public class Veiculo {

    public static final double CAPACIDADE_TANQUE_PADRAO_L = 45;

    private final Long id;
    private final String deviceId;
    private final String placa;
    private final String modelo;
    private final double capacidadeTanqueL;
    private final Double metaConsumoKml;
    private final String thingsboardToken;
    private boolean equipamentoOnline;
    private Instant ultimaComunicacao;
    private final Instant criadoEm;

    public Veiculo(Long id, String deviceId, String placa, String modelo, double capacidadeTanqueL,
                   Double metaConsumoKml, String thingsboardToken, boolean equipamentoOnline,
                   Instant ultimaComunicacao, Instant criadoEm) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId e obrigatorio");
        }
        if (capacidadeTanqueL <= 0) {
            throw new IllegalArgumentException("capacidadeTanqueL deve ser positiva");
        }
        this.id = id;
        this.deviceId = deviceId;
        this.placa = placa;
        this.modelo = modelo;
        this.capacidadeTanqueL = capacidadeTanqueL;
        this.metaConsumoKml = metaConsumoKml;
        this.thingsboardToken = thingsboardToken;
        this.equipamentoOnline = equipamentoOnline;
        this.ultimaComunicacao = ultimaComunicacao;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
    }

    public static Veiculo novo(String deviceId, String placa, String modelo, Double capacidadeTanqueL,
                               Double metaConsumoKml, String thingsboardToken, Instant criadoEm) {
        double capacidade = capacidadeTanqueL != null ? capacidadeTanqueL : CAPACIDADE_TANQUE_PADRAO_L;
        return new Veiculo(null, deviceId, placa, modelo, capacidade, metaConsumoKml,
                thingsboardToken, false, null, criadoEm);
    }

    /** Veiculo criado automaticamente na primeira mensagem de um equipamento desconhecido. */
    public static Veiculo autoCadastrado(String deviceId, Instant criadoEm) {
        return novo(deviceId, null, null, null, null, null, criadoEm);
    }

    public void registrarComunicacao(Instant instante) {
        equipamentoOnline = true;
        ultimaComunicacao = instante;
    }

    public void marcarOffline() {
        equipamentoOnline = false;
    }

    public boolean possuiTokenPainel() {
        return thingsboardToken != null && !thingsboardToken.isBlank();
    }

    public Long getId() { return id; }
    public String getDeviceId() { return deviceId; }
    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public double getCapacidadeTanqueL() { return capacidadeTanqueL; }
    public Double getMetaConsumoKml() { return metaConsumoKml; }
    public String getThingsboardToken() { return thingsboardToken; }
    public boolean isEquipamentoOnline() { return equipamentoOnline; }
    public Instant getUltimaComunicacao() { return ultimaComunicacao; }
    public Instant getCriadoEm() { return criadoEm; }
}
