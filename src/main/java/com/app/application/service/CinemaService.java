package com.app.application.service;

import com.app.application.dto.CinemaDto;
import com.app.application.dto.CreateCinemaDto;
import com.app.application.dto.CreateCinemaHallDto;
import com.app.application.exception.CinemaServiceException;
import com.app.application.service.util.ServiceUtils;
import com.app.application.validator.CreateCinemaDtoValidator;
import com.app.application.validator.CreateCinemaHallDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.cinema.Cinema;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final CityRepository cityRepository;
    private final CreateCinemaDtoValidator createCinemaDtoValidator;
    private final TransactionalOperator transactionalOperator;

    public Mono<CinemaDto> addCinema(CreateCinemaDto createCinemaDto) {

        var errors = createCinemaDtoValidator.validate(createCinemaDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new CinemaServiceException(Validations.createErrorMessage(errors)));
        }

        return cinemaHallRepository
                .addOrUpdateMany(createCinemaDto
                        .getCinemaHallsCapacity().stream()
                        .map(dtoVal -> CinemaHall.builder()
                                .positions(ServiceUtils.buildPositions(dtoVal.getRowNo(), dtoVal.getColNo()))
                                .movieEmissions(Collections.emptyList())
                                .build())
                        .collect(Collectors.toList()))
                .collectList()
                .flatMap(cinemaHalls -> cinemaRepository.addOrUpdate(Cinema.builder()
                        .cinemaHalls(cinemaHalls)
                        .street(createCinemaDto.getStreet())
                        .build()))
                .flatMap(cinema ->
                        cityRepository.findByName(createCinemaDto.getCity())
                                .switchIfEmpty(Mono.error(() -> new CinemaServiceException(
                                        "No city with name: %s".formatted(createCinemaDto.getCity()))))
                                .flatMap(city -> cinemaHallRepository
                                        .addOrUpdateMany(cinema.getCinemaHalls())
                                        .then(cityRepository.addOrUpdate(city.addCinema(cinema)))
                                        .then(cinemaRepository.addOrUpdate(
                                                cinema.setCity(city.getName()).setCinemasIdForCinemaHalls(cinema.getId())))))
                .map(Cinema::toDto)
                .as(transactionalOperator::transactional);
    }

    public Flux<CinemaDto> getAll() {
        return cinemaRepository.findAll()
                .map(Cinema::toDto);
    }

    public Flux<CinemaDto> getAllByCity(String city) {
        return cinemaRepository.findAllByCity(city)
                .map(Cinema::toDto);
    }

    public Mono<CinemaDto> addCinemaHallToCinema(String cinemaId, CreateCinemaHallDto createCinemaHallDto) {

        var errors = new CreateCinemaHallDtoValidator().validate(createCinemaHallDto);
        // Replaced synchronous throw before reactive chain with Mono.error()
        if (Validations.hasErrors(errors)) {
            return Mono.error(new CinemaServiceException(
                    "CreateCinemaHallDto is not valid. Errors are: [%s]".formatted(
                            Validations.createErrorMessage(errors))));
        }

        return cinemaRepository
                .findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new CinemaServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMap(cinema -> cinemaHallRepository
                        .addOrUpdate(createCinemaHallDto.toEntity(cinema.getId()))
                        .flatMap(savedCinemaHall -> cinemaRepository.addOrUpdate(
                                addCinemaHallToCinema(cinema, savedCinemaHall))))
                .map(Cinema::toDto)
                .as(transactionalOperator::transactional);
    }

    private Cinema addCinemaHallToCinema(Cinema cinema, CinemaHall cinemaHall) {
        cinema.getCinemaHalls().add(cinemaHall);
        return cinema;
    }
}
