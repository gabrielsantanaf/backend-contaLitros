package com.gabriel.backendcontalitros.application.port.in;

/**
 * Alteracao parcial do cadastro: campo nulo mantem o valor atual. Nos campos de
 * texto (placa, modelo e token), texto vazio remove o valor.
 */
public record AlteracaoVeiculo(
        String placa,
        String modelo,
        Double capacidadeTanqueL,
        Double metaConsumoKml,
        String thingsboardToken) {
}
