package com.gabriel.backendcontalitros.application.port.in;

public enum ResultadoProcessamento {
    ACEITA,
    /** vehicle_online = false: o equipamento esta ativo, mas os dados do veiculo estao congelados. */
    DESCARTADA_VEICULO_OFFLINE,
    /** Mesmo equipamento e mesmo ts ja gravados: duplicata gerada por reconexao. */
    DESCARTADA_DUPLICADA,
    /** Equipamento nao cadastrado e auto-cadastro desligado. */
    DESCARTADA_VEICULO_DESCONHECIDO
}
