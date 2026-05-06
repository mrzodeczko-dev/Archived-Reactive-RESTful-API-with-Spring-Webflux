package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.AddCinemaToCityDto;
import com.rzodeczko.application.dto.CityDto;
import com.rzodeczko.application.dto.CreateCityDto;
import com.rzodeczko.application.exception.CityServiceException;
import com.rzodeczko.application.mapper.CityMapper;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.service.util.ServiceUtils;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

public class CityService {

    private final CityPort cityPort;
    private final CinemaPort cinemaPort;
    private final CinemaHallPort cinemaHallPort;
    private final TransactionPort transactionPort;

    public CityService(CityPort cityPort, CinemaPort cinemaPort,
                       CinemaHallPort cinemaHallPort, TransactionPort transactionPort) {
        this.cityPort = cityPort;
        this.cinemaPort = cinemaPort;
        this.cinemaHallPort = cinemaHallPort;
        this.transactionPort = transactionPort;
    }

    public Mono<CityDto> addCity(Mono<CreateCityDto> createCityDto) {
        return createCityDto
                .flatMap(dto -> cityPort.addOrUpdate(dto.toEntity()))
                .map(CityMapper::toDto);
    }

    public Mono<CityDto> findByName(String name) {
        return cityPort.findByName(name)
                .switchIfEmpty(Mono.error(new CityServiceException("No city with name: %s".formatted(name))))
                .map(CityMapper::toDto);
    }

    public Mono<CityDto> addCinemaToCity(AddCinemaToCityDto addCinemaToCityDto) {

        Mono<City> result = cinemaHallPort.addOrUpdateMany(addCinemaToCityDto
                .getCinemaHallsCapacity().stream()
                .map(dtoVal -> CinemaHall.builder()
                        .positions(ServiceUtils.buildPositions(dtoVal.getRowNo(), dtoVal.getColNo()))
                        .movieEmissions(Collections.emptyList())
                        .build())
                .collect(Collectors.toList()))
                .collectList()
                .flatMap(cinemaHalls -> cinemaPort.addOrUpdate(Cinema.builder()
                        .cinemaHalls(cinemaHalls)
                        .build()))
                .flatMap(cinema ->
                        cityPort.findByName(addCinemaToCityDto.getCity())
                                .switchIfEmpty(Mono.error(() -> new CityServiceException(
                                        "No city with name: %s".formatted(addCinemaToCityDto.getCity()))))
                                .flatMap(cityEntity ->
                                        cinemaPort.addOrUpdate(cinema.setCity(cityEntity.getName()))
                                                .then(cityPort.addOrUpdate(cityEntity.addCinema(cinema)))));
        return transactionPort.inTransaction(result).map(CityMapper::toDto);
    }

    public Flux<CityDto> getAll() {
        return cityPort.findAll().map(CityMapper::toDto);
    }
}
