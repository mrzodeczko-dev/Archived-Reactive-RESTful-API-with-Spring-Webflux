package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.UserDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoAdminUserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByUsername(String username);
}
