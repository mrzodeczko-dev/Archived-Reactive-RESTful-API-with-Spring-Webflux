package com.app.application.service;

import com.app.application.exception.StatisticsServiceException;
import com.app.domain.cinema.Cinema;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.city.City;
import com.app.domain.city.CityRepository;
import com.app.domain.movie.Movie;
import com.app.domain.movie.MovieRepository;
import com.app.domain.movie_emission.MovieEmission;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket_purchase.TicketPurchase;
import com.app.domain.ticket_purchase.TicketPurchaseRepository;
import com.app.domain.vo.Discount;
import com.app.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private City warsaw;
    private City krakow;
    private Movie movie;
    private MovieEmission emission;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id("movie-1").name("Sample").genre("Drama").duration(120)
                .premiereDate(LocalDate.of(2025, 1, 1)).build();

        CinemaHall hall = CinemaHall.builder()
                .id("hall-1").positions(List.of()).movieEmissions(List.of()).build();

        Cinema cinema = Cinema.builder()
                .id("cinema-1").cinemaHalls(List.of(hall)).build();

        warsaw = City.builder().name("Warsaw").cinemas(List.of(cinema)).build();
        krakow = City.builder().name("Krakow").cinemas(List.of()).build();

        emission = MovieEmission.builder()
                .id("emission-1").movie(movie).baseTicketPrice(Money.of("50"))
                .cinemaHallId("hall-1").build();
    }

    private Ticket ticket(String price) {
        return Ticket.builder().price(Money.of(price)).discount(Discount.of("0.0")).build();
    }

    private TicketPurchase purchase(List<Ticket> tickets) {
        return TicketPurchase.builder()
                .id("tp-1").movieEmission(emission).tickets(tickets)
                .purchaseDate(LocalDate.now()).build();
    }

    @Test
    @DisplayName("findCitiesFrequency — emits per-city frequency from purchases")
    void findCitiesFrequency_shouldEmitFrequencies() {
        when(cityRepository.findAll()).thenReturn(Flux.just(warsaw));
        when(ticketPurchaseRepository.findAllByMovieEmissionInDateAndByCinemaHallsIdIn(any(LocalDate.class), anyList()))
                .thenReturn(Flux.just(purchase(List.of(ticket("10"), ticket("10")))));

        StepVerifier.create(statisticsService.findCitiesFrequency())
                .assertNext(dto -> {
                    assertThat(dto.getCity()).isEqualTo("Warsaw");
                    assertThat(dto.getFrequency()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findCitiesWithMostFrequency — completes empty when no cities exist")
    void findCitiesWithMostFrequency_whenNoCities_shouldEmitEmpty() {
        when(cityRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(statisticsService.findCitiesWithMostFrequency())
                .verifyComplete();
    }

    @Test
    @DisplayName("findCitiesWithMostFrequency — keeps only cities with the max frequency")
    void findCitiesWithMostFrequency_shouldFilterToMax() {
        Cinema krakowCinema = Cinema.builder().id("c2")
                .cinemaHalls(List.of(CinemaHall.builder().id("hall-2").positions(List.of()).movieEmissions(List.of()).build()))
                .build();
        City krakowWithCinema = City.builder().name("Krakow").cinemas(List.of(krakowCinema)).build();

        when(cityRepository.findAll()).thenReturn(Flux.just(warsaw, krakowWithCinema));
        // Warsaw -> 2 tickets, Krakow -> 1 ticket. Max should be Warsaw.
        when(ticketPurchaseRepository.findAllByMovieEmissionInDateAndByCinemaHallsIdIn(any(LocalDate.class), anyList()))
                .thenReturn(Flux.just(purchase(List.of(ticket("10"), ticket("10")))))
                .thenReturn(Flux.just(purchase(List.of(ticket("10")))));

        StepVerifier.create(statisticsService.findCitiesWithMostFrequency())
                .assertNext(dto -> {
                    assertThat(dto.getCity()).isEqualTo("Warsaw");
                    assertThat(dto.getFrequency()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllMoviesFrequency — emits frequency aggregated per movie")
    void findAllMoviesFrequency_shouldAggregate() {
        when(movieRepository.findAll()).thenReturn(Flux.just(movie));
        when(ticketPurchaseRepository.findAllByMovieId("movie-1"))
                .thenReturn(Flux.just(
                        purchase(List.of(ticket("10"), ticket("10"))),
                        purchase(List.of(ticket("10")))));

        StepVerifier.create(statisticsService.findAllMoviesFrequency())
                .assertNext(dto -> {
                    assertThat(dto.getMovie().getId()).isEqualTo("movie-1");
                    assertThat(dto.getFrequency()).isEqualTo(3);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findMostPopularMoviesGroupedByGenreInCity — emits error when city name is null")
    void findMostPopularMoviesGroupedByGenreInCity_whenNullCity_shouldEmitError() {
        StepVerifier.create(statisticsService.findMostPopularMoviesGroupedByGenreInCity(null))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(StatisticsServiceException.class)
                        .hasMessageContaining("City name is required"))
                .verify();
    }

    @Test
    @DisplayName("findMostPopularMoviesGroupedByGenreInCity — emits error when city is not found")
    void findMostPopularMoviesGroupedByGenreInCity_whenCityNotFound_shouldEmitError() {
        when(cityRepository.findByName("Unknown")).thenReturn(Mono.empty());

        StepVerifier.create(statisticsService.findMostPopularMoviesGroupedByGenreInCity("Unknown"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(StatisticsServiceException.class)
                        .hasMessageContaining("Unknown"))
                .verify();
    }

    @Test
    @DisplayName("findMostPopularMoviesGroupedByGenreInCity — emits genre frequency entries when city found")
    void findMostPopularMoviesGroupedByGenreInCity_whenCityFound_shouldEmit() {
        when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(warsaw));
        when(ticketPurchaseRepository.findAllByCinemaHallsIds(anyList()))
                .thenReturn(Flux.just(purchase(List.of(ticket("10"), ticket("10")))));

        StepVerifier.create(statisticsService.findMostPopularMoviesGroupedByGenreInCity("Warsaw"))
                .assertNext(dto -> {
                    assertThat(dto.getGenre()).isEqualTo("Drama");
                    assertThat(dto.getFrequency()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getAverageTicketPriceGroupedByCity — averages ticket prices per city")
    void getAverageTicketPriceGroupedByCity_shouldComputeAverage() {
        when(cityRepository.findAll()).thenReturn(Flux.just(warsaw));
        when(ticketPurchaseRepository.findAllByCinemaHallsIds(anyList()))
                .thenReturn(Flux.just(purchase(List.of(ticket("10"), ticket("20")))));

        StepVerifier.create(statisticsService.getAverageTicketPriceGroupedByCity())
                .assertNext(dto -> {
                    assertThat(dto.getCity()).isEqualTo("Warsaw");
                    assertThat(dto.getAverageTicketPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getAverageTicketPriceGroupedByCity — returns zero when no tickets")
    void getAverageTicketPriceGroupedByCity_whenNoTickets_shouldReturnZero() {
        when(cityRepository.findAll()).thenReturn(Flux.just(warsaw));
        when(ticketPurchaseRepository.findAllByCinemaHallsIds(anyList()))
                .thenReturn(Flux.empty());

        StepVerifier.create(statisticsService.getAverageTicketPriceGroupedByCity())
                .assertNext(dto -> {
                    assertThat(dto.getCity()).isEqualTo("Warsaw");
                    assertThat(dto.getAverageTicketPrice()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }
}
