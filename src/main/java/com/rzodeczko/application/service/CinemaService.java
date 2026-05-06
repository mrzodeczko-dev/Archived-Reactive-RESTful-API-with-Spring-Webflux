package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CinemaDto;
import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.exception.CinemaServiceException;
import com.rzodeczko.application.mapper.CinemaMapper;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.service.util.ServiceUtils;
import com.rzodeczko.application.validator.CreateCinemaDtoValidator;
import com.rzodeczko.application.validator.CreateCinemaHallDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

public class CinemaService {

    private final CinemaPort cinemaPort;
    private final CinemaHallPort cinemaHallPort;
    private final CityPort cityPort;
    private final CreateCinemaDtoValidator createCinemaDtoValidator;
    private final TransactionPort transactionPort;

    public CinemaService(CinemaPort cinemaPort, CinemaHallPort cinemaHallPort, CityPort cityPort,
                         CreateCinemaDtoValidator createCinemaDtoValidator, TransactionPort transactionPort) {
        this.cinemaPort = cinemaPort;
        this.cinemaHallPort = cinemaHallPort;
        this.cityPort = cityPort;
        this.createCinemaDtoValidator = createCinemaDtoValidator;
        this.transactionPort = transactionPort;
    }

    public Mono<CinemaDto> addCinema(CreateCinemaDto createCinemaDto) {

        var errors = createCinemaDtoValidator.validate(createCinemaDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new CinemaServiceException(Validations.createErrorMessage(errors)));
        }

        Mono<Cinema> result = cinemaHallPort
                .addOrUpdateMany(createCinemaDto
                        .getCinemaHallsCapacity().stream()
                        .map(dtoVal -> CinemaHall.builder()
                                .positions(ServiceUtils.buildPositions(dtoVal.getRowNo(), dtoVal.getColNo()))
                                .movieEmissions(Collections.emptyList())
                                .build())
                        .collect(Collectors.toList()))
                .collectList()
                .flatMap(cinemaHalls -> cinemaPort.addOrUpdate(Cinema.builder()
                        .cinemaHalls(cinemaHalls)
                        .street(createCinemaDto.getStreet())
                        .build()))
                .flatMap(cinema ->
                        cityPort.findByName(createCinemaDto.getCity())
                                .switchIfEmpty(Mono.error(() -> new CinemaServiceException(
                                        "No city with name: %s".formatted(createCinemaDto.getCity()))))
                                .flatMap(city -> cinemaPort
                                        .addOrUpdate(cinema.setCity(city.getName()).setCinemasIdForCinemaHalls(cinema.getId()))
                                        .flatMap(savedCinema -> cityPort.addOrUpdate(city.addCinema(savedCinema))
                                                .thenReturn(savedCinema))));
        return transactionPort.inTransaction(result).map(CinemaMapper::toDto);
    }

    public Flux<CinemaDto> getAll() {
        return cinemaPort.findAll().map(CinemaMapper::toDto);
    }

    public Flux<CinemaDto> getAllByCity(String city) {
        return cinemaPort.findAllByCity(city).map(CinemaMapper::toDto);
    }

    public Mono<CinemaDto> addCinemaHallToCinema(String cinemaId, CreateCinemaHallDto createCinemaHallDto) {

        var errors = new CreateCinemaHallDtoValidator().validate(createCinemaHallDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(new CinemaServiceException(
                    "CreateCinemaHallDto is not valid. Errors are: [%s]".formatted(
                            Validations.createErrorMessage(errors))));
        }

        Mono<Cinema> result = cinemaPort
                .findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new CinemaServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMap(cinema -> cinemaHallPort
                        .addOrUpdate(createCinemaHallDto.toEntity(cinema.getId()))
                        .flatMap(savedCinemaHall -> cinemaPort.addOrUpdate(addCinemaHallToCinema(cinema, savedCinemaHall))));
        return transactionPort.inTransaction(result).map(CinemaMapper::toDto);
    }

    private Cinema addCinemaHallToCinema(Cinema cinema, CinemaHall cinemaHall) {
        cinema.getCinemaHalls().add(cinemaHall);
        return cinema;
    }
}
