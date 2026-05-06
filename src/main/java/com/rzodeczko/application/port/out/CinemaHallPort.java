package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CinemaHallPort extends CrudPort<CinemaHall, String> {

    Flux<CinemaHall> getAllForCinemaById(String cinemaId);

    Mono<CinemaHall> getByMovieEmissionId(String movieEmissionId);
}
