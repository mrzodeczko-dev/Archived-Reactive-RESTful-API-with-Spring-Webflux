package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.CinemaDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MongoCinemaRepository extends ReactiveMongoRepository<CinemaDocument, String> {

    @Query(value = "{'cinemaHalls':{$elemMatch: {'_id': ?0}}}")
    Mono<CinemaDocument> findByCinemaHallId(String id);

    @Query("{'city': ?0}")
    Flux<CinemaDocument> getAllByCity(String city);
}
