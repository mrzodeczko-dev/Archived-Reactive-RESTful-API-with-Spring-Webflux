package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.application.dto.TicketPurchaseDto;
import com.rzodeczko.application.exception.TicketOrderServiceException;
import com.rzodeczko.application.exception.TicketPurchaseServiceException;
import com.rzodeczko.application.mapper.TicketPurchaseMapper;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.application.port.out.TicketOrderPort;
import com.rzodeczko.application.port.out.TicketPort;
import com.rzodeczko.application.port.out.TicketPurchasePort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.validator.CreateTicketPurchaseDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class TicketPurchaseService {

    private final TicketPurchasePort ticketPurchasePort;
    private final CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator;
    private final MovieEmissionPort movieEmissionPort;
    private final MoviePort moviePort;
    private final UserPort userPort;
    private final CinemaHallPort cinemaHallPort;
    private final TicketPort ticketPort;
    private final CityPort cityPort;
    private final TicketOrderPort ticketOrderPort;
    private final CinemaPort cinemaPort;
    private final TransactionPort transactionPort;

    public TicketPurchaseService(TicketPurchasePort ticketPurchasePort,
                                 CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator,
                                 MovieEmissionPort movieEmissionPort,
                                 MoviePort moviePort, UserPort userPort,
                                 CinemaHallPort cinemaHallPort, TicketPort ticketPort,
                                 CityPort cityPort, TicketOrderPort ticketOrderPort,
                                 CinemaPort cinemaPort, TransactionPort transactionPort) {
        this.ticketPurchasePort = ticketPurchasePort;
        this.createTicketPurchaseDtoValidator = createTicketPurchaseDtoValidator;
        this.movieEmissionPort = movieEmissionPort;
        this.moviePort = moviePort;
        this.userPort = userPort;
        this.cinemaHallPort = cinemaHallPort;
        this.ticketPort = ticketPort;
        this.cityPort = cityPort;
        this.ticketOrderPort = ticketOrderPort;
        this.cinemaPort = cinemaPort;
        this.transactionPort = transactionPort;
    }

    public Mono<TicketPurchaseDto> purchaseTicket(Mono<? extends Principal> principal, CreateTicketPurchaseDto createPurchaseDto) {

        var errors = createTicketPurchaseDtoValidator.validate(createPurchaseDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new TicketOrderServiceException(
                    "Validation errors: %s".formatted(Validations.createErrorMessage(errors))));
        }

        Mono<TicketPurchase> result = movieEmissionPort
                .findById(createPurchaseDto.getMovieEmissionId())
                .switchIfEmpty(Mono.error(() -> new TicketOrderServiceException(
                        "No movie emission with id: %s".formatted(createPurchaseDto.getMovieEmissionId()))))
                .flatMap(movieEmission ->
                        createPurchaseDto.areAllPositionsAvailable(movieEmission.getFreePositions())
                                ? Mono.just(movieEmission)
                                : Mono.error(new TicketOrderServiceException("Positions are not available")))
                .flatMap(movieEmission -> Mono.zip(
                        movieEmissionPort.addOrUpdate(movieEmission.removeFreePositions(
                                createPurchaseDto.getTicketsDetails().stream()
                                        .map(TicketDetailsDto::getPosition)
                                        .collect(Collectors.toList()))),
                        principal.flatMap(p -> userPort.findByUsername(p.getName()))
                ).map(tuple -> TicketPurchase.builder()
                        .purchaseDate(LocalDate.now())
                        .ticketGroupType(createPurchaseDto.getTicketGroupType())
                        .user(tuple.getT2())
                        .movieEmission(movieEmission)
                        .tickets(createPurchaseDto.getTicketsDetails()
                                .stream()
                                .map(ticketDetailsDto -> {
                                    Discount totalDiscount = createPurchaseDto.getBaseDiscount()
                                            .add(ticketDetailsDto.getIndividualTicketType().getDiscount());
                                    Money ticketPrice = computeTicketPrice(
                                            movieEmission.getBaseTicketPrice(), totalDiscount);
                                    return Ticket.builder()
                                            .position(ticketDetailsDto.getPosition())
                                            .type(ticketDetailsDto.getIndividualTicketType())
                                            .ticketStatus(TicketStatus.PURCHASED)
                                            .discount(totalDiscount)
                                            .price(ticketPrice)
                                            .build();
                                })
                                .collect(Collectors.toList()))
                        .build()))
                .flatMap(ticketPurchase ->
                        ticketPort.addOrUpdateMany(ticketPurchase.getTickets())
                                .then(ticketPurchasePort.addOrUpdate(ticketPurchase)));

        return transactionPort.inTransaction(result).map(TicketPurchaseMapper::toDto);
    }

    private Money computeTicketPrice(Money baseTicketPrice, Discount totalDiscount) {
        return baseTicketPrice.multiply(totalDiscount.inverse().getValue().toPlainString());
    }

    public Mono<TicketPurchaseDto> purchaseTicketFromOrder(String username, String ticketOrderId) {
        if (isNull(ticketOrderId)) {
            return Mono.error(new TicketPurchaseServiceException("Ticket order id is null"));
        }

        Mono<TicketPurchase> result = ticketOrderPort.findById(ticketOrderId)
                .switchIfEmpty(Mono.error(new TicketPurchaseServiceException(
                        "No ticket order with id: %s".formatted(ticketOrderId))))
                .flatMap(ticketOrder -> validateTicketOrderOwnership(ticketOrder, username))
                .flatMap(ticketOrder ->
                        ticketOrderPort.addOrUpdate(ticketOrder.changeOrderStatusToDone())
                                .then(ticketPurchasePort.addOrUpdate(ticketOrder.toTicketPurchase())));

        return transactionPort.inTransaction(result).map(TicketPurchaseMapper::toDto);
    }

    private Mono<TicketOrder> validateTicketOrderOwnership(TicketOrder ticketOrder, String username) {
        if (!Objects.equals(username, ticketOrder.getUser().getUsername())) {
            return Mono.error(new TicketPurchaseServiceException("Ticket order is not done by you!"));
        }
        if (ticketOrder.getMovieEmission().getStartDateTime().toLocalDate()
                .compareTo(LocalDate.now().minusDays(1)) < 0) {
            return Mono.error(new TicketPurchaseServiceException("You cannot purchase ticket 1 day before emission"));
        }
        return Mono.just(ticketOrder);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByUser(String username) {
        return ticketPurchasePort
                .findAllByUserUsername(username)
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByUserAndCity(String username, String cityName) {
        return getAllTicketPurchasesByCityUtility(cityName, ticketPurchasePort.findAllByUserUsername(username));
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCity(String cityName) {
        return getAllTicketPurchasesByCityUtility(cityName, ticketPurchasePort.findAll());
    }

    private Flux<TicketPurchaseDto> getAllTicketPurchasesByCityUtility(String cityName, Flux<TicketPurchase> ticketPurchases) {
        if (isNull(cityName) || StringUtils.isBlank(cityName)) {
            return Flux.error(() -> new TicketPurchaseServiceException("City name is required and cannot be empty"));
        }

        return cityPort.findByName(cityName)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No city with name %s".formatted(cityName))))
                .map(city -> (Set<String>) city.getCinemas()
                        .stream()
                        .flatMap(cinema -> cinema.getCinemaHalls().stream())
                        .map(CinemaHall::getId)
                        .collect(Collectors.toSet()))
                .flatMapMany(cinemaHallIds -> ticketPurchases
                        .filter(tp -> cinemaHallIds.contains(tp.getMovieEmission().getCinemaHallId())))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchaseByCinema(String cinemaId) {
        if (isNull(cinemaId) || StringUtils.isBlank(cinemaId)) {
            return Flux.error(() -> new TicketPurchaseServiceException("Cinema id is required and cannot be empty"));
        }

        return cinemaPort.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> ticketPurchasePort
                        .findAllByCinemaHallsIds(
                                cinema.getCinemaHalls().stream()
                                        .map(CinemaHall::getId)
                                        .collect(Collectors.toList())))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCinemaAndUsername(String cinemaId, String username) {
        if (isNull(cinemaId) || StringUtils.isBlank(cinemaId)) {
            return Flux.error(() -> new TicketPurchaseServiceException("Cinema id is required and cannot be empty"));
        }

        return cinemaPort.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> ticketPurchasePort
                        .findAllByCinemaHallsIdsAndUsername(
                                cinema.getCinemaHalls().stream()
                                        .map(CinemaHall::getId)
                                        .collect(Collectors.toList()), username))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchases() {
        return ticketPurchasePort
                .findAll()
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByDate(Optional<String> from, Optional<String> to) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        if (from.isEmpty() && to.isEmpty()) {
            return Flux.error(new TicketPurchaseServiceException("At least one of dates [from, to] is required!"));
        }

        boolean validFrom = from.map(s -> GenericValidator.isDate(s, "dd-MM-yyyy", true)).orElse(true);
        boolean validTo = to.map(s -> GenericValidator.isDate(s, "dd-MM-yyyy", true)).orElse(true);

        if (!validFrom && !validTo) {
            return Flux.error(new TicketPurchaseServiceException("Date from and date to has not valid format"));
        }
        if (!validFrom) {
            return Flux.error(new TicketPurchaseServiceException("Date from has not valid format"));
        }
        if (!validTo) {
            return Flux.error(new TicketPurchaseServiceException("Date to has not valid format"));
        }

        LocalDate fromDate = from.map(s -> LocalDate.parse(s, fmt)).orElse(null);
        LocalDate toDate = to.map(s -> LocalDate.parse(s, fmt)).orElse(null);

        if (fromDate != null && toDate != null && fromDate.compareTo(toDate) > 0) {
            return Flux.error(new TicketPurchaseServiceException("From date cannot be after to date!"));
        }

        if (fromDate != null && toDate != null) {
            return ticketPurchasePort.findAllByPurchaseDateBetween(fromDate, toDate)
                    .map(TicketPurchaseMapper::toDto);
        }
        if (fromDate != null) {
            return ticketPurchasePort.findAllByPurchaseDateAfter(fromDate)
                    .map(TicketPurchaseMapper::toDto);
        }
        return ticketPurchasePort.findAllByPurchaseDateBefore(toDate)
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesWithMovieId(String movieId) {
        return moviePort
                .findById(movieId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No movie with id: %s".formatted(movieId))))
                .flatMapMany(movie -> ticketPurchasePort.findAllByMovieId(movie.getId()))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesForUsernameAndMovieId(String username, String movieId) {
        return moviePort
                .findById(movieId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No movie with id: %s".formatted(movieId))))
                .flatMapMany(movie -> ticketPurchasePort
                        .findAllByMovieIdAndUserUsername(movie.getId(), username))
                .map(TicketPurchaseMapper::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCinemaHallId(String cinemaHallId) {
        return cinemaHallPort
                .findById(cinemaHallId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema hall with id: %s".formatted(cinemaHallId))))
                .flatMapMany(cinemaHall -> ticketPurchasePort.findAllByCinemaHallId(cinemaHall.getId()))
                .map(TicketPurchaseMapper::toDto);
    }
}
