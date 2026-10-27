package com.gabriel.backendcontalitros.application.port.in;

public interface FecharViagensInativasUseCase {

    /**
     * Fecha as viagens abertas que nao recebem leitura ha mais que o tempo limite.
     *
     * @return quantidade de viagens fechadas
     */
    int fecharInativas();
}
