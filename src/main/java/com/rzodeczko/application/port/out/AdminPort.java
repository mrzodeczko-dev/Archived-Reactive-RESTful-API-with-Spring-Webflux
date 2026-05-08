package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.security.Admin;
import reactor.core.publisher.Mono;

public interface AdminPort extends PersistencePort<Admin, String> {
    Mono<Admin> findByUsername(String username);
}
