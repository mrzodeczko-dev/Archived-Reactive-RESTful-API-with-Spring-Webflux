package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.exception.MovieEmissionServiceException;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.service.MovieEmissionService;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieEmissionServiceTest {

    @Mock
    private MovieEmissionPort movieEmissionRepository;
    @Mock
    private CinemaHallPort cinemaHallRepository;
    @Mock
    private MoviePort movieRepository;
    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private MovieEmissionService movieEmissionService;

    private Movie movie;
    private CinemaHall hall;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id("movie-1")
                .name("Sample")
                .genre("Drama")
                .duration(2)
                .premiereDate(LocalDate.of(2025, 1, 1))
                .build();

        hall = CinemaHall.builder()
                .id("hall-1")
                .cinemaId("cinema-1")
                .positions(List.of(new Position(1, 1)))
                .movieEmissions(new ArrayList<>())
                .build();

        // Mock TransactionalOperator: pass the publisher through unchanged.
        Mockito.lenient().when(transactionPort.inTransaction(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("createMovieEmission — emits validation error for invalid dto")
    void createMovieEmission_whenInvalid_shouldEmitError() {
        CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder().build();

        StepVerifier.create(movieEmissionService.createMovieEmission(dto))
                .expectError(MovieEmissionServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("createMovieEmission — emits error when movie not found")
    void createMovieEmission_whenMovieNotFound_shouldEmitError() {
        CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                .movieId("missing").cinemaHallId("hall-1")
                .startTime("2030-06-01 18:00").baseTicketPrice("50.00").build();

        when(movieRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(movieEmissionService.createMovieEmission(dto))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(MovieEmissionServiceException.class)
                        .hasMessageContaining("missing"))
                .verify();
    }

    @Test
    @DisplayName("createMovieEmission — emits error when start time is before premiere")
    void createMovieEmission_whenBeforePremiere_shouldEmitError() {
        Movie future = Movie.builder()
                .id("movie-1").name("Future").genre("Drama").duration(2)
                .premiereDate(LocalDate.of(2035, 1, 1)).build();
        CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                .movieId("movie-1").cinemaHallId("hall-1")
                .startTime("2027-06-01 18:00").baseTicketPrice("50.00").build();

        when(movieRepository.findById("movie-1")).thenReturn(Mono.just(future));

        StepVerifier.create(movieEmissionService.createMovieEmission(dto))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(MovieEmissionServiceException.class)
                        .hasMessageContaining("before premiere"))
                .verify();
    }

    @Test
    @DisplayName("createMovieEmission — emits error when cinema hall not found")
    void createMovieEmission_whenHallNotFound_shouldEmitError() {
        CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                .movieId("movie-1").cinemaHallId("missing-hall")
                .startTime("2030-06-01 18:00").baseTicketPrice("50.00").build();

        when(movieRepository.findById("movie-1")).thenReturn(Mono.just(movie));
        when(cinemaHallRepository.findById("missing-hall")).thenReturn(Mono.empty());

        StepVerifier.create(movieEmissionService.createMovieEmission(dto))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(MovieEmissionServiceException.class)
                        .hasMessageContaining("missing-hall"))
                .verify();
    }

    @Test
    @DisplayName("createMovieEmission — happy path returns emission dto")
    void createMovieEmission_whenValid_shouldReturnDto() {
        CreateMovieEmissionDto dto = CreateMovieEmissionDto.builder()
                .movieId("movie-1").cinemaHallId("hall-1")
                .startTime("2030-06-01 18:00").baseTicketPrice("50.00").build();

        when(movieRepository.findById("movie-1")).thenReturn(Mono.just(movie));
        when(cinemaHallRepository.findById("hall-1")).thenReturn(Mono.just(hall));

        MovieEmission saved = MovieEmission.builder()
                .id("emission-1").movie(movie)
                .startDateTime(LocalDateTime.of(2030, 6, 1, 18, 0))
                .baseTicketPrice(Money.of("50")).cinemaHallId("hall-1")
                .isPositionFree(new HashMap<>()).build();
        when(movieEmissionRepository.addOrUpdate(any(MovieEmission.class)))
                .thenReturn(Mono.just(saved));
        when(cinemaHallRepository.addOrUpdate(any(CinemaHall.class)))
                .thenReturn(Mono.just(hall));

        StepVerifier.create(movieEmissionService.createMovieEmission(dto))
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo("emission-1");
                    assertThat(result.getCinemaHallId()).isEqualTo("hall-1");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllMovieEmissions — emits all emissions as dto")
    void getAllMovieEmissions_shouldEmit() {
        MovieEmission emission = MovieEmission.builder()
                .id("e-1").movie(movie)
                .startDateTime(LocalDateTime.of(2030, 6, 1, 18, 0))
                .baseTicketPrice(Money.of("50"))
                .cinemaHallId("hall-1").isPositionFree(new HashMap<>()).build();
        when(movieEmissionRepository.findAll()).thenReturn(Flux.just(emission));

        StepVerifier.create(movieEmissionService.getAllMovieEmissions())
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("e-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllMovieEmissionsByMovieId — emits error when no emissions found")
    void getAllMovieEmissionsByMovieId_whenEmpty_shouldEmitError() {
        when(movieEmissionRepository.findMovieEmissionsByMovieId("movie-1"))
                .thenReturn(Flux.empty());

        StepVerifier.create(movieEmissionService.getAllMovieEmissionsByMovieId("movie-1"))
                .expectError(MovieEmissionServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("getAllMovieEmissionsByCinemaHallId — emits error when no emissions found")
    void getAllMovieEmissionsByCinemaHallId_whenEmpty_shouldEmitError() {
        when(movieEmissionRepository.findMovieEmissionsByCinemaHallId("hall-1"))
                .thenReturn(Flux.empty());

        StepVerifier.create(movieEmissionService.getAllMovieEmissionsByCinemaHallId("hall-1"))
                .expectError(MovieEmissionServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("deleteMovieEmission — emits error when emission not found")
    void deleteMovieEmission_whenNotFound_shouldEmitError() {
        Mockito.lenient().when(transactionPort.inTransaction(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(movieEmissionRepository.deleteById("missing"))
                .thenReturn(Mono.empty());

        StepVerifier.create(movieEmissionService.deleteMovieEmission("missing"))
                .expectError(MovieEmissionServiceException.class)
                .verify();
    }

    @Test
    @DisplayName("deleteMovieEmission — happy path returns deleted emission dto")
    void deleteMovieEmission_whenFound_shouldReturnDto() {
        MovieEmission emission = MovieEmission.builder()
                .id("e-1").movie(movie)
                .startDateTime(LocalDateTime.of(2030, 6, 1, 18, 0))
                .baseTicketPrice(Money.of("50"))
                .cinemaHallId("hall-1").isPositionFree(new HashMap<>()).build();

        when(movieEmissionRepository.deleteById("e-1")).thenReturn(Mono.just(emission));
        when(cinemaHallRepository.getByMovieEmissionId("e-1")).thenReturn(Mono.just(hall));
        when(cinemaHallRepository.addOrUpdate(any(CinemaHall.class))).thenReturn(Mono.just(hall));

        StepVerifier.create(movieEmissionService.deleteMovieEmission("e-1"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("e-1"))
                .verifyComplete();
    }
}
