package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MongoMovieRepository extends ReactiveMongoRepository<MovieDocument, String> {
    Mono<MovieDocument> findByNameAndGenre(String name, String genre);
    Flux<MovieDocument> findAllByGenre(String genre);
    Flux<MovieDocument> findAllByName(String name);
    Flux<MovieDocument> findAllByDurationBetween(int min, int max);
    Flux<MovieDocument> findAllByDurationGreaterThanEqual(int min);
    Flux<MovieDocument> findAllByDurationLessThanEqual(int max);
    Flux<MovieDocument> findAllByPremiereDateBetween(LocalDate from, LocalDate to);
    Flux<MovieDocument> findAllByPremiereDateGreaterThanEqual(LocalDate from);
    Flux<MovieDocument> findAllByPremiereDateLessThanEqual(LocalDate to);
}
