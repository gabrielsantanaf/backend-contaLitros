package com.gabriel.backendcontalitros.application.port.out;

import java.util.function.Supplier;

/** Delimita uma transacao de banco sem acoplar a aplicacao ao Spring. */
public interface TransacaoPort {

    <T> T executar(Supplier<T> trabalho);

    default void executarSemRetorno(Runnable trabalho) {
        executar(() -> {
            trabalho.run();
            return null;
        });
    }
}
