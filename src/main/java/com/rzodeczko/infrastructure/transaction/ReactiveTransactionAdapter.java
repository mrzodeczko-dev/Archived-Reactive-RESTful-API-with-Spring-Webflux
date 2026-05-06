package com.rzodeczko.infrastructure.transaction;

import com.rzodeczko.application.port.out.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReactiveTransactionAdapter implements TransactionPort {

    private final TransactionalOperator transactionalOperator;

    public ReactiveTransactionAdapter(TransactionalOperator transactionalOperator) {
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public <T> Mono<T> inTransaction(Mono<T> mono) {
        return transactionalOperator.transactional(mono);
    }

    @Override
    public <T> Flux<T> inTransactionMany(Flux<T> flux) {
        return transactionalOperator.transactional(flux);
    }
}
