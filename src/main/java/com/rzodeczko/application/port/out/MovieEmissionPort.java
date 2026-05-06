package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.movie_emission.MovieEmission;
import reactor.core.publisher.Flux;

public interface MovieEmissionPort extends CrudPort<MovieEmission, String> {

    Flux<MovieEmission> findMovieEmissionsByMovieId(String movieId);

    Flux<MovieEmission> findMovieEmissionsByCinemaHallId(String cinemaHallId);
}
