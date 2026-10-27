package com.gabriel.backendcontalitros.infrastructure.persistence;

import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransacaoSpringAdapter implements TransacaoPort {

    private final TransactionTemplate template;

    public TransacaoSpringAdapter(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T executar(Supplier<T> trabalho) {
        return template.execute(status -> trabalho.get());
    }
}
