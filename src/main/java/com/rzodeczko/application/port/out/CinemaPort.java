package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.cinema.Cinema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CinemaPort extends CrudPort<Cinema, String> {

    Mono<Cinema> findByCinemaHallId(String id);

    Flux<Cinema> findAllByCity(String city);
}
