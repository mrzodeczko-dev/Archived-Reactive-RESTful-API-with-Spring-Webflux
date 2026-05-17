package com.rzodeczko.application.port.out;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserPort extends PersistencePort<User, String> {
    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);

    default Mono<User> deleteUserByUsername(String username) {
        return findByUsername(username)
                .filter(user -> user.getRole() != Role.ROLE_ADMIN)
                .flatMap(user -> deleteById(user.getId()).thenReturn(user));
    }

    default Flux<User> deleteAllExceptAdmins() {
        return findAll()
                .filter(user -> user.getRole() != Role.ROLE_ADMIN)
                .flatMap(user -> deleteById(user.getId())
                        .thenReturn(user));
    }
}
