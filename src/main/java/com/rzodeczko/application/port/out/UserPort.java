package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.security.User;
import reactor.core.publisher.Mono;

public interface UserPort extends CrudPort<User, String> {
    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);
}
