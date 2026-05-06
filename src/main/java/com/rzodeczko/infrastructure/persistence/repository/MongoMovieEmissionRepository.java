package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MongoMovieEmissionRepository extends ReactiveMongoRepository<MovieEmissionDocument, String> {

    Flux<MovieEmissionDocument> findMovieEmissionsByMovieId(String movieId);

    Flux<MovieEmissionDocument> findMovieEmissionsByCinemaHallId(String cinemaHallId);
}
