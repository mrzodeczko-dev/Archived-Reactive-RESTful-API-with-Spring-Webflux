package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserPort extends PersistencePort<User, String> {
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<User> deleteByUsername(String username);
    default Flux<User> deleteAll() {
        return findAll().flatMap(user -> deleteById(user.getId()));
    }
}
