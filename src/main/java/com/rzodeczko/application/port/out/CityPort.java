package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.city.City;
import reactor.core.publisher.Mono;

public interface CityPort extends PersistencePort<City, String> {

    Mono<City> findByName(String name);
}
