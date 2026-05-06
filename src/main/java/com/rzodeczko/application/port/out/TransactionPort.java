package com.rzodeczko.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionPort {
    <T> Mono<T> inTransaction(Mono<T> mono);
    <T> Flux<T> inTransactionMany(Flux<T> flux);
}
