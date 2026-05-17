package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoUserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByUsername(String username);

    Mono<UserDocument> findByEmail(String email);

    default Mono<UserDocument> deleteByUsername(String username) {
        return findByUsername(username)
                .filter(user -> user.getRole() != Role.ROLE_ADMIN)
                .flatMap(userDocument -> deleteById(userDocument.getId()).thenReturn(userDocument));
    }
}
