package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.AddCinemaToCityDto;
import com.rzodeczko.application.dto.CityDto;
import com.rzodeczko.application.dto.CreateCityDto;
import com.rzodeczko.application.exception.CityServiceException;
import com.rzodeczko.application.mapper.CityMapper;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.CityCsvParserPort;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.service.util.ServiceUtils;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CityService {

    private final CityPort cityPort;
    private final CinemaPort cinemaPort;
    private final CinemaHallPort cinemaHallPort;
    private final CityCsvParserPort cityCsvParserPort;
    private final TransactionPort transactionPort;

    public CityService(CityPort cityPort, CinemaPort cinemaPort,
                       CinemaHallPort cinemaHallPort, CityCsvParserPort cityCsvParserPort,
                       TransactionPort transactionPort) {
        this.cityPort = cityPort;
        this.cinemaPort = cinemaPort;
        this.cinemaHallPort = cinemaHallPort;
        this.cityCsvParserPort = cityCsvParserPort;
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
                        .cinemaHallsCapacity().stream()
                        .map(dtoVal -> CinemaHall.builder()
                                .positions(ServiceUtils.buildPositions(dtoVal.rowNo(), dtoVal.colNo()))
                                .movieEmissions(Collections.emptyList())
                                .build())
                        .collect(Collectors.toList()))
                .collectList()
                .flatMap(cinemaHalls -> cinemaPort.addOrUpdate(Cinema.builder()
                        .cinemaHalls(cinemaHalls)
                        .build()))
                .flatMap(cinema ->
                        cityPort.findByName(addCinemaToCityDto.city())
                                .switchIfEmpty(Mono.error(() -> new CityServiceException(
                                        "No city with name: %s".formatted(addCinemaToCityDto.city()))))
                                .flatMap(cityEntity -> {
                                    var cinemaWithCity = cinema.setCityId(cityEntity.getName());
                                    return cinemaPort.addOrUpdate(cinemaWithCity)
                                            .then(cityPort.addOrUpdate(cityEntity.addCinema(cinemaWithCity)));
                                }));
        return transactionPort.inTransaction(result).map(CityMapper::toDto);
    }

    public Flux<CityDto> getAll() {
        return cityPort.findAll().map(CityMapper::toDto);
    }

    public Flux<CityDto> uploadCSVFile(InputStream inputStream) {
        return cityCsvParserPort.parse(inputStream)
                .flatMapMany(parseResult -> {
                    if (parseResult.hasErrors()) {
                        return Flux.error(new CityServiceException("Errors are: %s".formatted(parseResult.errors())));
                    }

                    var cities = parseResult.items().stream()
                            .map(CreateCityDto::toEntity)
                            .toList();
                    return Flux.fromIterable(cities)
                            .index()
                            .concatMap(indexed -> cityPort.findByName(indexed.getT2().getName())
                                    .hasElement()
                                    .mapNotNull(exists -> exists
                                            ? "City in row no. %s is not unique by name".formatted(indexed.getT1() + 1)
                                            : null))
                            .collectList()
                            .flatMapMany(results -> {
                                var errors = results.stream().toList();
                                if (!errors.isEmpty()) {
                                    return Flux.error(new CityServiceException("Errors are: %s".formatted(errors)));
                                }
                                return transactionPort
                                        .inTransactionMany(cityPort.addOrUpdateMany(cities))
                                        .map(CityMapper::toDto);
                            });
                });
    }
}
