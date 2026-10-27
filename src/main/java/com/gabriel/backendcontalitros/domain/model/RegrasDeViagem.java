package com.gabriel.backendcontalitros.domain.model;

import java.time.Instant;

/**
 * Regras que decidem o que acontece com a viagem quando chega uma leitura.
 *
 * <ul>
 *   <li>Distancia voltou para tras: fecha a viagem atual (REINICIO_EQUIPAMENTO).</li>
 *   <li>Ignicao ligada sem viagem aberta: abre uma viagem.</li>
 *   <li>Ignicao ligada com viagem aberta: atualiza o recorte.</li>
 *   <li>Ignicao desligada com viagem aberta: atualiza e fecha (IGNICAO_DESLIGADA).</li>
 * </ul>
 */
public final class RegrasDeViagem {

    private RegrasDeViagem() {
    }

    /** A qual viagem a leitura que provocou a transicao pertence. */
    public enum VinculoLeitura { VIAGEM_FECHADA, VIAGEM_ABERTA, NENHUM }

    /**
     * @param fechada viagem fechada por esta leitura (ou null)
     * @param aberta  viagem que segue aberta apos esta leitura (ou null)
     * @param vinculo a qual das duas a leitura pertence
     */
    public record Transicao(Viagem fechada, Viagem aberta, VinculoLeitura vinculo) {
    }

    public static Transicao aplicar(Long veiculoId, Viagem viagemAberta, DadosTelemetria dados, Instant instante) {
        Viagem fechada = null;
        Viagem aberta = viagemAberta;

        if (aberta != null && aberta.indicaReinicio(dados)) {
            aberta.fechar(MotivoFechamento.REINICIO_EQUIPAMENTO, instante);
            fechada = aberta;
            aberta = null;
        }

        if (dados.ignicao()) {
            if (aberta == null) {
                aberta = Viagem.abrir(veiculoId, dados, instante);
            } else {
                aberta.atualizar(dados, instante);
            }
            return new Transicao(fechada, aberta, VinculoLeitura.VIAGEM_ABERTA);
        }

        if (aberta != null) {
            aberta.atualizar(dados, instante);
            aberta.fechar(MotivoFechamento.IGNICAO_DESLIGADA, instante);
            return new Transicao(aberta, null, VinculoLeitura.VIAGEM_FECHADA);
        }

        // Ignicao desligada sem viagem aberta (ou logo apos um reinicio): a leitura fica avulsa.
        return new Transicao(fechada, null, VinculoLeitura.NENHUM);
    }
}
