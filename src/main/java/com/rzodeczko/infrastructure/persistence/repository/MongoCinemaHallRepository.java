package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.CinemaHallDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MongoCinemaHallRepository extends ReactiveMongoRepository<CinemaHallDocument, String> {

    @Query(value = "{'cinemaId': ?0}")
    Flux<CinemaHallDocument> findByCinemaId(String cinemaId);

    @Query(value = "{'movieEmissions':{$elemMatch: {'_id': ?0}}}")
    Mono<CinemaHallDocument> findByMovieEmissionId(String id);
}
