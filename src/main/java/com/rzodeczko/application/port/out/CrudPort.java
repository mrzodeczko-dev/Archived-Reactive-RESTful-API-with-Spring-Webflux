package com.rzodeczko.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CrudPort<T, ID> {
    Mono<T> addOrUpdate(T item);
    Flux<T> addOrUpdateMany(List<T> items);
    Flux<T> findAll();
    Mono<T> findById(ID id);
    Flux<T> findAllById(List<ID> ids);
    Mono<T> deleteById(ID id);
    Flux<T> deleteAllById(List<ID> ids);
}
