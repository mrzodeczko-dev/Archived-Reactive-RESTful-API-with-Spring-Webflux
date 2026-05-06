package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
import com.rzodeczko.application.exception.TicketOrderServiceException;
import com.rzodeczko.application.exception.TicketPurchaseServiceException;
import com.rzodeczko.application.port.out.*;
import com.rzodeczko.application.service.TicketPurchaseService;
import com.rzodeczko.application.validator.CreateTicketPurchaseDtoValidator;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketPurchaseServiceTest {

    @Mock
    private TicketPurchasePort ticketPurchasePort;
    @Mock
    private CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator;
    @Mock
    private MovieEmissionPort movieEmissionPort;
    @Mock
    private MoviePort moviePort;
    @Mock
    private CinemaHallPort cinemaHallPort;
    @Mock
    private CityPort cityPort;
    @Mock
    private TicketOrderPort ticketOrderPort;
    @Mock
    private CinemaPort cinemaPort;
    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private TicketPurchaseService ticketPurchaseService;

    private Movie movie;
    private MovieEmission emission;
    private TicketPurchase samplePurchase;
    private User user;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id("movie-1").name("Sample").genre("Drama").duration(2)
                .premiereDate(LocalDate.of(2025, 1, 1)).build();

        emission = MovieEmission.builder()
                .id("emission-1").movie(movie).baseTicketPrice(Money.of("50"))
                .cinemaHallId("hall-1")
                .startDateTime(LocalDateTime.now().plusDays(5))
                .isPositionFree(new HashMap<>()).build();

        user = User.builder().username("alice").build();

        samplePurchase = TicketPurchase.builder()
                .id("tp-1").movieEmission(emission).user(user)
                .tickets(List.of()).purchaseDate(LocalDate.now()).build();

        Mockito.lenient().when(transactionPort.inTransaction(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("purchaseTicket — emits validation error when validator finds issues")
    void purchaseTicket_whenInvalid_shouldEmitError() {
        CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                .movieEmissionId("emission-1").build();
        java.util.Map<String, String> errors = new HashMap<>();
        errors.put("movieEmissionId", "is required");
        when(createTicketPurchaseDtoValidator.validate(dto)).thenReturn(errors);

        StepVerifier.create(ticketPurchaseService.purchaseTicket(Mono.empty(), dto))
                .expectError(TicketOrderServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("purchaseTicket — emits error when movie emission not found")
    void purchaseTicket_whenEmissionNotFound_shouldEmitError() {
        CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                .movieEmissionId("missing").ticketsDetails(List.of())
                .ticketGroupType(TicketGroupType.NORMAL).build();

        when(createTicketPurchaseDtoValidator.validate(dto)).thenReturn(new HashMap<>());
        when(movieEmissionPort.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(ticketPurchaseService.purchaseTicket(Mono.empty(), dto))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketOrderServiceException.class)
                        .hasMessageContaining("missing"))
                .verify();
    }

    @Test
    @DisplayName("purchaseTicketFromOrder — emits error when ticket order id is null")
    void purchaseTicketFromOrder_whenNullId_shouldEmitError() {
        StepVerifier.create(ticketPurchaseService.purchaseTicketFromOrder("alice", null))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketPurchaseServiceException.class)
                        .hasMessageContaining("null"))
                .verify();
    }

    @Test
    @DisplayName("purchaseTicketFromOrder — emits error when order not found")
    void purchaseTicketFromOrder_whenOrderNotFound_shouldEmitError() {
        when(ticketOrderPort.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(ticketPurchaseService.purchaseTicketFromOrder("alice", "missing"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketPurchaseServiceException.class)
                        .hasMessageContaining("No ticket order with id"))
                .verify();
    }

    @Test
    @DisplayName("purchaseTicketFromOrder — emits error when user does not own the order")
    void purchaseTicketFromOrder_whenNotOwner_shouldEmitError() {
        TicketOrder order = TicketOrder.builder()
                .id("order-1").user(User.builder().username("bob").build())
                .movieEmission(emission).tickets(List.of())
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .ticketGroupType(TicketGroupType.NORMAL).build();
        when(ticketOrderPort.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(ticketPurchaseService.purchaseTicketFromOrder("alice", "order-1"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketPurchaseServiceException.class)
                        .hasMessageContaining("not done by you"))
                .verify();
    }

    @Test
    @DisplayName("purchaseTicketFromOrder — emits error when emission already past")
    void purchaseTicketFromOrder_whenPastEmission_shouldEmitError() {
        MovieEmission past = MovieEmission.builder()
                .id("e-old").movie(movie).baseTicketPrice(Money.of("50"))
                .cinemaHallId("hall-1")
                .startDateTime(LocalDateTime.now().minusDays(5))
                .isPositionFree(new HashMap<>()).build();
        TicketOrder order = TicketOrder.builder()
                .id("order-1").user(user).movieEmission(past).tickets(List.of())
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .ticketGroupType(TicketGroupType.NORMAL).build();
        when(ticketOrderPort.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(ticketPurchaseService.purchaseTicketFromOrder("alice", "order-1"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketPurchaseServiceException.class)
                        .hasMessageContaining("1 day before emission"))
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByUser — emits purchases for user")
    void getAllTicketPurchasesByUser_shouldEmitPurchases() {
        when(ticketPurchasePort.findAllByUserUsername("alice"))
                .thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByUser("alice"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("tp-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByUserAndCity — emits error when city name is null")
    void getAllTicketPurchasesByUserAndCity_whenNullCity_shouldEmitError() {
        when(ticketPurchasePort.findAllByUserUsername("alice"))
                .thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByUserAndCity("alice", null))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByUserAndCity — emits error when city not found")
    void getAllTicketPurchasesByUserAndCity_whenCityMissing_shouldEmitError() {
        when(ticketPurchasePort.findAllByUserUsername("alice"))
                .thenReturn(Flux.just(samplePurchase));
        when(cityPort.findByName("Unknown")).thenReturn(Mono.empty());

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByUserAndCity("alice", "Unknown"))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchaseByCinema — emits error when cinemaId is null")
    void getAllTicketPurchaseByCinema_whenNullId_shouldEmitError() {
        StepVerifier.create(ticketPurchaseService.getAllTicketPurchaseByCinema(null))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchaseByCinema — emits error when cinema not found")
    void getAllTicketPurchaseByCinema_whenNotFound_shouldEmitError() {
        when(cinemaPort.findById("c-missing")).thenReturn(Mono.empty());

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchaseByCinema("c-missing"))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchaseByCinema — emits purchases for cinema")
    void getAllTicketPurchaseByCinema_whenFound_shouldEmit() {
        Cinema cinema = Cinema.builder().id("c-1")
                .cinemaHalls(List.of(CinemaHall.builder().id("hall-1")
                        .positions(List.of()).movieEmissions(List.of()).build()))
                .build();
        when(cinemaPort.findById("c-1")).thenReturn(Mono.just(cinema));
        when(ticketPurchasePort.findAllByCinemaHallsIds(anyList()))
                .thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchaseByCinema("c-1"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("tp-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByDate — emits error when both dates empty")
    void getAllTicketPurchasesByDate_whenBothEmpty_shouldEmitError() {
        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByDate(Optional.empty(), Optional.empty()))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByDate — emits error when from date format invalid")
    void getAllTicketPurchasesByDate_whenInvalidFrom_shouldEmitError() {
        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByDate(
                        Optional.of("not-a-date"), Optional.empty()))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketPurchaseServiceException.class)
                        .hasMessageContaining("Date from has not valid format"))
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByDate — emits error when from is after to")
    void getAllTicketPurchasesByDate_whenFromAfterTo_shouldEmitError() {
        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByDate(
                        Optional.of("10-12-2025"), Optional.of("01-12-2025")))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketPurchaseServiceException.class)
                        .hasMessageContaining("From date cannot be after to date"))
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByDate — happy path with both dates uses between query")
    void getAllTicketPurchasesByDate_whenBothValid_shouldUseBetween() {
        when(ticketPurchasePort.findAllByPurchaseDateBetween(
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 10)))
                .thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByDate(
                        Optional.of("01-12-2025"), Optional.of("10-12-2025")))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("tp-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByDate — only from uses 'after' query")
    void getAllTicketPurchasesByDate_whenOnlyFrom_shouldUseAfter() {
        when(ticketPurchasePort.findAllByPurchaseDateAfter(LocalDate.of(2025, 12, 1)))
                .thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByDate(
                        Optional.of("01-12-2025"), Optional.empty()))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("tp-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketPurchasesWithMovieId — emits error when movie not found")
    void getAllTicketPurchasesWithMovieId_whenMovieMissing_shouldEmitError() {
        when(moviePort.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesWithMovieId("missing"))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchasesWithMovieId — emits purchases when movie found")
    void getAllTicketPurchasesWithMovieId_whenMovieFound_shouldEmit() {
        when(moviePort.findById("movie-1")).thenReturn(Mono.just(movie));
        when(ticketPurchasePort.findAllByMovieId("movie-1"))
                .thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesWithMovieId("movie-1"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("tp-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketPurchasesByCinemaHallId — emits error when hall not found")
    void getAllTicketPurchasesByCinemaHallId_whenHallMissing_shouldEmitError() {
        when(cinemaHallPort.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchasesByCinemaHallId("missing"))
                .expectError(TicketPurchaseServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllTicketPurchases — emits all purchases")
    void getAllTicketPurchases_shouldEmit() {
        when(ticketPurchasePort.findAll()).thenReturn(Flux.just(samplePurchase));

        StepVerifier.create(ticketPurchaseService.getAllTicketPurchases())
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("tp-1"))
                .verifyComplete();
    }
}
