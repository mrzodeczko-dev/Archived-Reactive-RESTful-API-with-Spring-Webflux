package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.CityDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoCityRepository extends ReactiveMongoRepository<CityDocument, String> {
    @Query("{'name':?0}")
    Mono<CityDocument> findByName(String name);
}
