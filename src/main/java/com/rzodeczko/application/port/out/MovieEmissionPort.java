package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.movie_emission.MovieEmission;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

public interface MovieEmissionPort extends PersistencePort<MovieEmission, String> {

    Flux<MovieEmission> findMovieEmissionsByMovieId(String movieId);

    Flux<MovieEmission> findMovieEmissionsByCinemaHallId(String cinemaHallId);

    Flux<MovieEmission> findMovieEmissionsByCinemaHallIdInAndStartDateTimeBetweenAndMovieId(List<String> cinemaHallId, LocalDateTime startDateTime, LocalDateTime toDate, String movieId);
}
