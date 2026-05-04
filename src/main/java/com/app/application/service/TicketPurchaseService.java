package com.app.application.service;

import com.app.application.dto.CreateTicketPurchaseDto;
import com.app.application.dto.TicketPurchaseDto;
import com.app.application.exception.TicketOrderServiceException;
import com.app.application.exception.TicketPurchaseServiceException;
import com.app.application.validator.CreateTicketPurchaseDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.CityRepository;
import com.app.domain.movie.MovieRepository;
import com.app.domain.movie_emission.MovieEmission;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.security.UserRepository;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket.TicketRepository;
import com.app.domain.ticket.enums.TicketStatus;
import com.app.domain.ticket_order.TicketOrder;
import com.app.domain.ticket_order.TicketOrderRepository;
import com.app.domain.ticket_purchase.TicketPurchase;
import com.app.domain.ticket_purchase.TicketPurchaseRepository;
import com.app.domain.vo.Discount;
import com.app.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
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

@Service
@RequiredArgsConstructor
public class TicketPurchaseService {

    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator;
    private final MovieEmissionRepository movieEmissionRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final TicketRepository ticketRepository;
    private final CityRepository cityRepository;
    private final TicketOrderRepository ticketOrderRepository;
    private final CinemaRepository cinemaRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<TicketPurchaseDto> purchaseTicket(Mono<? extends Principal> principal, CreateTicketPurchaseDto createPurchaseDto) {

        var errors = createTicketPurchaseDtoValidator.validate(createPurchaseDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new TicketOrderServiceException(
                    "Validation errors: %s".formatted(Validations.createErrorMessage(errors))));
        }

        return movieEmissionRepository
                .findById(createPurchaseDto.getMovieEmissionId())
                .switchIfEmpty(Mono.error(() -> new TicketOrderServiceException(
                        "No movie emission with id: %s".formatted(createPurchaseDto.getMovieEmissionId()))))
                .flatMap(movieEmission ->
                        createPurchaseDto.areAllPositionsAvailable(movieEmission.getFreePositions())
                                ? Mono.just(movieEmission)
                                : Mono.error(new TicketOrderServiceException("Positions are not available")))
                .flatMap(movieEmission -> Mono.zip(
                        movieEmissionRepository.addOrUpdate(movieEmission.removeFreePositions(createPurchaseDto.getTicketsDetails())),
                        principal.flatMap(p -> userRepository.findByUsername(p.getName()))
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
                        ticketRepository.addOrUpdateMany(ticketPurchase.getTickets())
                                .then(ticketPurchaseRepository.addOrUpdate(ticketPurchase))
                                .map(TicketPurchase::toDto))
                .as(transactionalOperator::transactional);
    }

    /**
     * Computes the final ticket price by applying the total discount to the base ticket price.
     * Formula: finalPrice = basePrice * (1 - totalDiscount)
     */
    private Money computeTicketPrice(Money baseTicketPrice, Discount totalDiscount) {
        return baseTicketPrice.multiply(totalDiscount.inverse().getValue().toPlainString());
    }

    public Mono<TicketPurchaseDto> purchaseTicketFromOrder(String username, String ticketOrderId) {
        if (isNull(ticketOrderId)) {
            return Mono.error(new TicketPurchaseServiceException("Ticket order id is null"));
        }

        return ticketOrderRepository.findById(ticketOrderId)
                .switchIfEmpty(Mono.error(new TicketPurchaseServiceException(
                        "No ticket order with id: %s".formatted(ticketOrderId))))
                .flatMap(ticketOrder -> validateTicketOrderOwnership(ticketOrder, username))
                .flatMap(ticketOrder ->
                        ticketOrderRepository.addOrUpdate(ticketOrder.changeOrderStatusToDone())
                                .then(ticketPurchaseRepository.addOrUpdate(ticketOrder.toTicketPurchase())))
                .map(TicketPurchase::toDto)
                .as(transactionalOperator::transactional);
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
        return ticketPurchaseRepository
                .findAllByUserUsername(username)
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByUserAndCity(String username, String cityName) {
        return getAllTicketPurchasesByCityUtility(cityName, ticketPurchaseRepository.findAllByUserUsername(username));
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCity(String cityName) {
        return getAllTicketPurchasesByCityUtility(cityName, ticketPurchaseRepository.findAll());
    }

    private Flux<TicketPurchaseDto> getAllTicketPurchasesByCityUtility(String cityName, Flux<TicketPurchase> ticketPurchases) {
        if (isNull(cityName) || StringUtils.isBlank(cityName)) {
            return Flux.error(() -> new TicketPurchaseServiceException("City name is required and cannot be empty"));
        }

        return cityRepository.findByName(cityName)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No city with name %s".formatted(cityName))))
                .map(city -> (Set<String>) city.getCinemas()
                        .stream()
                        .flatMap(cinema -> cinema.getCinemaHalls().stream())
                        .map(CinemaHall::getId)
                        .collect(Collectors.toSet()))
                .flatMapMany(cinemaHallIds -> ticketPurchases
                        .filter(tp -> cinemaHallIds.contains(tp.getMovieEmission().getCinemaHallId())))
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchaseByCinema(String cinemaId) {
        if (isNull(cinemaId) || StringUtils.isBlank(cinemaId)) {
            return Flux.error(() -> new TicketPurchaseServiceException("Cinema id is required and cannot be empty"));
        }

        return cinemaRepository.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> ticketPurchaseRepository
                        .findAllByCinemaHallsIds(
                                cinema.getCinemaHalls().stream()
                                        .map(CinemaHall::getId)
                                        .collect(Collectors.toList())))
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCinemaAndUsername(String cinemaId, String username) {
        if (isNull(cinemaId) || StringUtils.isBlank(cinemaId)) {
            return Flux.error(() -> new TicketPurchaseServiceException("Cinema id is required and cannot be empty"));
        }

        return cinemaRepository.findById(cinemaId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema with id: %s".formatted(cinemaId))))
                .flatMapMany(cinema -> ticketPurchaseRepository
                        .findAllByCinemaHallsIdsAndUsername(
                                cinema.getCinemaHalls().stream()
                                        .map(CinemaHall::getId)
                                        .collect(Collectors.toList()), username))
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchases() {
        return ticketPurchaseRepository
                .findAll()
                .map(TicketPurchase::toDto);
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
            return ticketPurchaseRepository.findAllByPurchaseDateBetween(fromDate, toDate)
                    .map(TicketPurchase::toDto);
        }
        if (fromDate != null) {
            return ticketPurchaseRepository.findAllByPurchaseDateAfter(fromDate)
                    .map(TicketPurchase::toDto);
        }
        return ticketPurchaseRepository.findAllByPurchaseDateBefore(toDate)
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesWithMovieId(String movieId) {
        return movieRepository
                .findById(movieId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No movie with id: %s".formatted(movieId))))
                .flatMapMany(movie -> ticketPurchaseRepository.findAllByMovieId(movie.getId()))
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesForUsernameAndMovieId(String username, String movieId) {
        return movieRepository
                .findById(movieId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No movie with id: %s".formatted(movieId))))
                .flatMapMany(movie -> ticketPurchaseRepository
                        .findAllByMovieIdAndUserUsername(movie.getId(), username))
                .map(TicketPurchase::toDto);
    }

    public Flux<TicketPurchaseDto> getAllTicketPurchasesByCinemaHallId(String cinemaHallId) {
        return cinemaHallRepository
                .findById(cinemaHallId)
                .switchIfEmpty(Mono.error(() -> new TicketPurchaseServiceException(
                        "No cinema hall with id: %s".formatted(cinemaHallId))))
                .flatMapMany(cinemaHall -> ticketPurchaseRepository.findAllByCinemaHallId(cinemaHall.getId()))
                .map(TicketPurchase::toDto);
    }
}
