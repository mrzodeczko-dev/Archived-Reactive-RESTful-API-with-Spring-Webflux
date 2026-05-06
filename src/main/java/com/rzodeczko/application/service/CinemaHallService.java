package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.AddCinemaHallToCinemaDto;
import com.rzodeczko.application.dto.CinemaHallDto;
import com.rzodeczko.application.exception.CinemaHallServiceException;
import com.rzodeczko.application.mapper.CinemaHallMapper;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.validator.AddCinemaHallToCinemaDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.vo.Position;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CinemaHallService {

    private final CinemaHallPort cinemaHallPort;
    private final CinemaPort cinemaPort;
    private final TransactionPort transactionPort;

    public CinemaHallService(CinemaHallPort cinemaHallPort, CinemaPort cinemaPort, TransactionPort transactionPort) {
        this.cinemaHallPort = cinemaHallPort;
        this.cinemaPort = cinemaPort;
        this.transactionPort = transactionPort;
    }

    public Mono<CinemaHallDto> addCinemaHallToCinema(Mono<AddCinemaHallToCinemaDto> addCinemaHallToCinemaDtoMono) {

        Mono<CinemaHall> result = addCinemaHallToCinemaDtoMono
                .flatMap(dto -> {
                    var errors = new AddCinemaHallToCinemaDtoValidator().validate(dto);
                    if (Validations.hasErrors(errors)) {
                        return Mono.error(new CinemaHallServiceException(Validations.createErrorMessage(errors)));
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> cinemaPort.findById(dto.getCinemaId())
                        .switchIfEmpty(Mono.error(new CinemaHallServiceException(
                                "No cinema with id: %s".formatted(dto.getCinemaId()))))
                        .flatMap(cinema -> cinemaHallPort.addOrUpdate(CinemaHall.builder()
                                .cinemaId(cinema.getId())
                                .movieEmissions(new ArrayList<>())
                                .positions(createPositions(dto.getColNo(), dto.getRowNo()))
                                .build())
                                .flatMap(savedCinemaHall -> {
                                    cinema.getCinemaHalls().add(savedCinemaHall);
                                    return cinemaPort.addOrUpdate(cinema)
                                            .thenReturn(savedCinemaHall);
                                })
                        )
                );
        return transactionPort.inTransaction(result).map(CinemaHallMapper::toDto);
    }

    private List<Position> createPositions(Integer colNo, Integer rowNo) {
        return IntStream.rangeClosed(1, colNo)
                .boxed()
                .collect(ArrayList::new, (list, col) -> IntStream
                                .rangeClosed(1, rowNo)
                                .boxed()
                                .map(row -> Position.builder()
                                        .colNo(col)
                                        .rowNo(row)
                                        .build())
                                .forEach(list::add),
                        List::addAll);
    }

    public Flux<CinemaHallDto> getAll() {
        return cinemaHallPort.findAll().map(CinemaHallMapper::toDto);
    }

    public Flux<CinemaHallDto> getAllForCinema(String cinemaId) {
        return cinemaPort.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new CinemaHallServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> cinemaHallPort.getAllForCinemaById(cinemaId)
                        .map(CinemaHallMapper::toDto));
    }
}
