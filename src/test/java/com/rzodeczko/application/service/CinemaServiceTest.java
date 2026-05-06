package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.exception.CinemaServiceException;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.service.CinemaService;
import com.rzodeczko.application.validator.CreateCinemaDtoValidator;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaPort cinemaRepository;
    @Mock
    private CinemaHallPort cinemaHallRepository;
    @Mock
    private CityPort cityRepository;
    @Mock
    private CreateCinemaDtoValidator createCinemaDtoValidator;
    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private CinemaService cinemaService;

    private CinemaHall cinemaHall;
    private Cinema cinema;
    private City city;

    @BeforeEach
    void setUp() {
        cinemaHall = CinemaHall.builder().id("hall-1").positions(new ArrayList<>()).movieEmissions(new ArrayList<>()).build();
        cinema = Cinema.builder().id("cinema-1").street("Main St").cinemaHalls(new ArrayList<>(List.of(cinemaHall))).build();
        city = City.builder().id("city-1").name("Warsaw").cinemas(new ArrayList<>()).build();
    }

    @Nested
    @DisplayName("addCinema()")
    class AddCinemaTests {

        @Test
        @DisplayName("Happy path: valid DTO creates cinema with halls and links to city")
        void shouldAddCinemaSuccessfully() {
            CreateCinemaDto dto = CreateCinemaDto.builder()
                    .city("Warsaw")
                    .street("Main St")
                    .cinemaHallsCapacity(List.of(CreateCinemaHallDto.builder().rowNo(5).colNo(5).build()))
                    .build();

            when(createCinemaDtoValidator.validate(dto)).thenReturn(Map.of());
            when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(cinemaHall));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(city));
            when(cityRepository.addOrUpdate(any())).thenReturn(Mono.just(city));
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaService.addCinema(dto))
                    .assertNext(result -> {
                        assertThat(result.getId()).isEqualTo("cinema-1");
                        assertThat(result.getStreet()).isEqualTo("Main St");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Validation error: validator returns errors, Mono.error emitted")
        void shouldEmitErrorWhenValidationFails() {
            CreateCinemaDto dto = CreateCinemaDto.builder().build();
            when(createCinemaDtoValidator.validate(dto)).thenReturn(Map.of("street", "must not be blank"));

            StepVerifier.create(cinemaService.addCinema(dto))
                    .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(CinemaServiceException.class))
                    .verify();

            verifyNoInteractions(cinemaHallRepository, cinemaRepository, cityRepository);
        }

        @Test
        @DisplayName("City not found: CinemaServiceException with city name")
        void shouldThrowWhenCityNotFound() {
            CreateCinemaDto dto = CreateCinemaDto.builder()
                    .city("Atlantis")
                    .street("Main St")
                    .cinemaHallsCapacity(List.of(CreateCinemaHallDto.builder().rowNo(3).colNo(3).build()))
                    .build();

            when(createCinemaDtoValidator.validate(dto)).thenReturn(Map.of());
            when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(cinemaHall));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(cityRepository.findByName("Atlantis")).thenReturn(Mono.empty());
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaService.addCinema(dto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CinemaServiceException.class);
                        assertThat(ex.getMessage()).contains("Atlantis");
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Two cinemas: Flux emits two DTOs")
        void shouldReturnAll() {
            Cinema cinema2 = Cinema.builder().id("cinema-2").street("Side St").cinemaHalls(new ArrayList<>()).build();
            when(cinemaRepository.findAll()).thenReturn(Flux.just(cinema, cinema2));

            StepVerifier.create(cinemaService.getAll())
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("cinema-1"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("cinema-2"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No cinemas: Flux completes empty")
        void shouldReturnEmptyFlux() {
            when(cinemaRepository.findAll()).thenReturn(Flux.empty());
            StepVerifier.create(cinemaService.getAll()).verifyComplete();
        }
    }

    @Nested
    @DisplayName("getAllByCity()")
    class GetAllByCityTests {

        @Test
        @DisplayName("Happy path: returns cinemas in given city")
        void shouldReturnCinemasForCity() {
            when(cinemaRepository.findAllByCity("Warsaw")).thenReturn(Flux.just(cinema));

            StepVerifier.create(cinemaService.getAllByCity("Warsaw"))
                    .assertNext(dto -> assertThat(dto.getId()).isEqualTo("cinema-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No cinemas in city: Flux completes empty")
        void shouldReturnEmptyWhenNoCinemasInCity() {
            when(cinemaRepository.findAllByCity("Nowhere")).thenReturn(Flux.empty());
            StepVerifier.create(cinemaService.getAllByCity("Nowhere")).verifyComplete();
        }
    }

    @Nested
    @DisplayName("addCinemaHallToCinema()")
    class AddCinemaHallToCinemaTests {

        @Test
        @DisplayName("Happy path: cinema hall added, cinema updated")
        void shouldAddCinemaHallSuccessfully() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder().rowNo(7).colNo(8).build();

            Cinema emptyCinema = Cinema.builder()
                    .id("cinema-1")
                    .city("Warszawa")
                    .cinemaHalls(new ArrayList<>())
                    .build();

            CinemaHall newHall = CinemaHall.builder()
                    .id("hall-new")
                    .cinemaId("cinema-1")
                    .positions(List.of())
                    .build();

            Cinema updatedCinema = Cinema.builder()
                    .id("cinema-1")
                    .city("Warszawa")
                    .cinemaHalls(List.of(newHall))
                    .build();

            when(cinemaRepository.findById("cinema-1")).thenReturn(Mono.just(emptyCinema));
            when(cinemaHallRepository.addOrUpdate(any())).thenReturn(Mono.just(newHall));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(updatedCinema));
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaService.addCinemaHallToCinema("cinema-1", dto))
                    .assertNext(result -> assertThat(result.getId()).isEqualTo("cinema-1"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Validation error: rowNo=0 emits CinemaServiceException in Mono")
        void shouldEmitValidationErrorOnInvalidDto() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder().rowNo(0).colNo(4).build();

            StepVerifier.create(cinemaService.addCinemaHallToCinema("cinema-1", dto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CinemaServiceException.class);
                        assertThat(ex.getMessage()).contains("CreateCinemaHallDto is not valid");
                    })
                    .verify();

            verifyNoInteractions(cinemaRepository, cinemaHallRepository);
        }

        @Test
        @DisplayName("Cinema not found: CinemaServiceException with cinema id")
        void shouldThrowWhenCinemaNotFound() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder().rowNo(6).colNo(6).build();

            when(cinemaRepository.findById("missing")).thenReturn(Mono.empty());
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cinemaService.addCinemaHallToCinema("missing", dto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CinemaServiceException.class);
                        assertThat(ex.getMessage()).contains("missing");
                    })
                    .verify();
        }
    }
}
