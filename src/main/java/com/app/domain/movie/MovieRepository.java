package com.app.domain.movie;

import com.app.domain.generic.CrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MovieRepository extends CrudRepository<Movie, String> {
    Mono<Movie> findByNameAndGenre(String name, String genre);
    Flux<Movie> findAllByGenre(String genre);
    Flux<Movie> findAllByName(String name);
    Flux<Movie> findAllByDurationBetween(int min, int max);
    Flux<Movie> findAllByDurationGreaterThanEqual(int min);
    Flux<Movie> findAllByDurationLessThanEqual(int max);
    Flux<Movie> findAllByPremiereDateBetween(LocalDate from, LocalDate to);
    Flux<Movie> findAllByPremiereDateGreaterThanEqual(LocalDate from);
    Flux<Movie> findAllByPremiereDateLessThanEqual(LocalDate to);
}
