package com.gabriel.backendcontalitros.domain.model;

public enum MotivoFechamento {
    /** A leitura chegou com a ignicao desligada. */
    IGNICAO_DESLIGADA,
    /** A distancia acumulada do equipamento voltou para tras: ele foi reiniciado. */
    REINICIO_EQUIPAMENTO,
    /** O broker entregou o Last Will (status offline) do equipamento. */
    EQUIPAMENTO_OFFLINE,
    /** Nenhuma mensagem chegou dentro do tempo limite de inatividade. */
    SEM_COMUNICACAO
}
