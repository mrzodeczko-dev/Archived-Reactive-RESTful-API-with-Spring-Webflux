package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.movie.Movie;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MoviePort extends CrudPort<Movie, String> {
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
