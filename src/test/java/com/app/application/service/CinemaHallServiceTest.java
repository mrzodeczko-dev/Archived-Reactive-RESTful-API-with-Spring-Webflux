package com.app.application.service;

import com.app.application.dto.AddCinemaHallToCinemaDto;
import com.app.application.exception.CinemaHallServiceException;
import com.app.domain.cinema.Cinema;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaHallServiceTest {

    @Mock
    private CinemaHallRepository cinemaHallRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private CinemaHallService cinemaHallService;

    private Cinema cinema;
    private CinemaHall cinemaHall;

    @BeforeEach
    void setUp() {
        cinemaHall = CinemaHall.builder()
                .id("hall-1")
                .cinemaId("cinema-1")
                .positions(new ArrayList<>())
                .movieEmissions(new ArrayList<>())
                .build();

        cinema = Cinema.builder()
                .id("cinema-1")
                .street("Main St")
                .cinemaHalls(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("addCinemaHallToCinema()")
    class AddCinemaHallToCinemaTests {

        @Test
        @DisplayName("Happy path: valid DTO creates hall and updates cinema")
        void shouldAddHallSuccessfully() {
            AddCinemaHallToCinemaDto dto = AddCinemaHallToCinemaDto.builder()
                    .cinemaId("cinema-1")
                    .rowNo(3)
                    .colNo(4)
                    .build();

            when(cinemaRepository.findById("cinema-1")).thenReturn(Mono.just(cinema));
            when(cinemaHallRepository.addOrUpdate(any())).thenReturn(Mono.just(cinemaHall));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaHallService.addCinemaHallToCinema(Mono.just(dto)))
                    .assertNext(result -> assertThat(result.getId()).isEqualTo("hall-1"))
                    .verifyComplete();

            verify(cinemaHallRepository).addOrUpdate(any());
            verify(cinemaRepository).addOrUpdate(any());
        }

        @Test
        @DisplayName("Validation error: null cinemaId throws CinemaHallServiceException")
        void shouldThrowWhenCinemaIdIsNull() {
            // Validator only checks cinemaId — supply null to actually trigger validation error
            AddCinemaHallToCinemaDto dto = AddCinemaHallToCinemaDto.builder()
                    .cinemaId(null)
                    .rowNo(3)
                    .colNo(4)
                    .build();

            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaHallService.addCinemaHallToCinema(Mono.just(dto)))
                    .expectError(CinemaHallServiceException.class)
                    .verify();

            verifyNoInteractions(cinemaRepository, cinemaHallRepository);
        }

        @Test
        @DisplayName("Cinema not found: switchIfEmpty triggers CinemaHallServiceException")
        void shouldThrowWhenCinemaNotFound() {
            AddCinemaHallToCinemaDto dto = AddCinemaHallToCinemaDto.builder()
                    .cinemaId("no-cinema")
                    .rowNo(3)
                    .colNo(3)
                    .build();

            when(cinemaRepository.findById("no-cinema")).thenReturn(Mono.empty());
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaHallService.addCinemaHallToCinema(Mono.just(dto)))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CinemaHallServiceException.class);
                        assertThat(ex.getMessage()).contains("no-cinema");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Position count: 3 cols × 2 rows creates 6 positions")
        void shouldCreateCorrectNumberOfPositions() {
            AddCinemaHallToCinemaDto dto = AddCinemaHallToCinemaDto.builder()
                    .cinemaId("cinema-1")
                    .rowNo(2)
                    .colNo(3)
                    .build();

            when(cinemaRepository.findById("cinema-1")).thenReturn(Mono.just(cinema));
            when(cinemaHallRepository.addOrUpdate(any(CinemaHall.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            var captor = org.mockito.ArgumentCaptor.forClass(CinemaHall.class);

            StepVerifier.create(cinemaHallService.addCinemaHallToCinema(Mono.just(dto)))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(cinemaHallRepository).addOrUpdate(captor.capture());
            assertThat(captor.getValue().getPositions()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Two halls: Flux emits two DTOs")
        void shouldReturnAll() {
            CinemaHall hall2 = CinemaHall.builder().id("hall-2").cinemaId("cinema-1")
                    .positions(new ArrayList<>()).movieEmissions(new ArrayList<>()).build();
            when(cinemaHallRepository.findAll()).thenReturn(Flux.just(cinemaHall, hall2));

            StepVerifier.create(cinemaHallService.getAll())
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("hall-1"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("hall-2"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No halls: Flux completes empty")
        void shouldReturnEmptyFlux() {
            when(cinemaHallRepository.findAll()).thenReturn(Flux.empty());
            StepVerifier.create(cinemaHallService.getAll()).verifyComplete();
        }
    }

    @Nested
    @DisplayName("getAllForCinema()")
    class GetAllForCinemaTests {

        @Test
        @DisplayName("Happy path: cinema exists, returns its halls")
        void shouldReturnHallsForCinema() {
            when(cinemaRepository.findById("cinema-1")).thenReturn(Mono.just(cinema));
            when(cinemaHallRepository.getAllForCinemaById("cinema-1")).thenReturn(Flux.just(cinemaHall));

            StepVerifier.create(cinemaHallService.getAllForCinema("cinema-1"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("hall-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Cinema not found: CinemaHallServiceException with cinema id")
        void shouldThrowWhenCinemaNotFound() {
            when(cinemaRepository.findById("no-cinema")).thenReturn(Mono.empty());

            StepVerifier.create(cinemaHallService.getAllForCinema("no-cinema"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CinemaHallServiceException.class);
                        assertThat(ex.getMessage()).contains("no-cinema");
                    })
                    .verify();
        }
    }
}
