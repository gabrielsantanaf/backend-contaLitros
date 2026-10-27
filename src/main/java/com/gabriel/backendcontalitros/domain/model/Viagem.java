package com.gabriel.backendcontalitros.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Uma viagem e um RECORTE dos contadores continuos do equipamento. Ao abrir,
 * guarda os contadores daquele instante como base; a partir dai o valor da
 * viagem e sempre {@code atual - base}. Se uma mensagem se perder, a proxima
 * traz o valor absoluto correto e a viagem se corrige sozinha.
 */
public class Viagem {

    private final Long id;
    private final Long veiculoId;
    private StatusViagem status;
    private final Instant inicio;
    private Instant fim;
    private Instant ultimaLeituraEm;
    private MotivoFechamento motivoFechamento;
    private Contadores totais;
    private Double notaEficiencia;
    private final Contadores base;

    public Viagem(Long id, Long veiculoId, StatusViagem status, Instant inicio, Instant fim,
                  Instant ultimaLeituraEm, MotivoFechamento motivoFechamento, Contadores totais,
                  Double notaEficiencia, Contadores base) {
        this.id = id;
        this.veiculoId = Objects.requireNonNull(veiculoId, "veiculoId");
        this.status = Objects.requireNonNull(status, "status");
        this.inicio = Objects.requireNonNull(inicio, "inicio");
        this.fim = fim;
        this.ultimaLeituraEm = Objects.requireNonNull(ultimaLeituraEm, "ultimaLeituraEm");
        this.motivoFechamento = motivoFechamento;
        this.totais = Objects.requireNonNull(totais, "totais");
        this.notaEficiencia = notaEficiencia;
        this.base = Objects.requireNonNull(base, "base");
    }

    public static Viagem abrir(Long veiculoId, DadosTelemetria dados, Instant instante) {
        return new Viagem(null, veiculoId, StatusViagem.ABERTA, instante, null, instante, null,
                Contadores.ZERO, dados.notaEficiencia(), dados.contadores());
    }

    public void atualizar(DadosTelemetria dados, Instant instante) {
        exigirAberta();
        totais = dados.contadores().desde(base);
        notaEficiencia = dados.notaEficiencia();
        ultimaLeituraEm = instante;
    }

    public void fechar(MotivoFechamento motivo, Instant instante) {
        exigirAberta();
        status = StatusViagem.FECHADA;
        motivoFechamento = Objects.requireNonNull(motivo, "motivo");
        fim = instante;
    }

    /** A distancia do equipamento ficou abaixo da base: o equipamento reiniciou os contadores. */
    public boolean indicaReinicio(DadosTelemetria dados) {
        return dados.contadores().distanciaKm() < base.distanciaKm();
    }

    public boolean estaAberta() {
        return status == StatusViagem.ABERTA;
    }

    /** Consumo medio calculado (nao armazenado): distancia / combustivel. */
    public Double consumoMedioKml() {
        return totais.combustivelL() > 0 ? totais.distanciaKm() / totais.combustivelL() : null;
    }

    private void exigirAberta() {
        if (!estaAberta()) {
            throw new IllegalStateException("A viagem " + id + " ja esta fechada");
        }
    }

    public Long getId() { return id; }
    public Long getVeiculoId() { return veiculoId; }
    public StatusViagem getStatus() { return status; }
    public Instant getInicio() { return inicio; }
    public Instant getFim() { return fim; }
    public Instant getUltimaLeituraEm() { return ultimaLeituraEm; }
    public MotivoFechamento getMotivoFechamento() { return motivoFechamento; }
    public Contadores getTotais() { return totais; }
    public Double getNotaEficiencia() { return notaEficiencia; }
    public Contadores getBase() { return base; }
}
