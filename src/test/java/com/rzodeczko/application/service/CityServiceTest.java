package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.AddCinemaToCityDto;
import com.rzodeczko.application.dto.CityDto;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.dto.CreateCityDto;
import com.rzodeczko.application.exception.CityServiceException;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.service.CityService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityPort cityPort;
    @Mock
    private CinemaPort cinemaPort;
    @Mock
    private CinemaHallPort cinemaHallPort;
    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private CityService cityService;

    private City cityWarsaw;
    private CityDto cityWarsawDto;

    @BeforeEach
    void setUp() {
        cityWarsaw = City.builder()
                .id("city-1")
                .name("Warsaw")
                .cinemas(new ArrayList<>())
                .build();

        cityWarsawDto = CityDto.builder()
                .id("city-1")
                .name("Warsaw")
                .build();
    }

    @Nested
    @DisplayName("addCity()")
    class AddCityTests {

        @Test
        @DisplayName("Happy path: valid DTO is persisted and mapped to CityDto")
        void shouldAddCitySuccessfully() {
            CreateCityDto dto = CreateCityDto.builder().name("Warsaw").build();
            when(cityPort.addOrUpdate(any())).thenReturn(Mono.just(cityWarsaw));

            StepVerifier.create(cityService.addCity(Mono.just(dto)))
                    .assertNext(result -> assertThat(result.getName()).isEqualTo("Warsaw"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Port error: exception propagated from addOrUpdate")
        void shouldPropagatePortError() {
            CreateCityDto dto = CreateCityDto.builder().name("Warsaw").build();
            when(cityPort.addOrUpdate(any()))
                    .thenReturn(Mono.error(new RuntimeException("DB down")));

            StepVerifier.create(cityService.addCity(Mono.just(dto)))
                    .expectErrorMessage("DB down")
                    .verify();
        }
    }

    @Nested
    @DisplayName("findByName()")
    class FindByNameTests {

        @Test
        @DisplayName("Happy path: existing city is returned as DTO")
        void shouldReturnCityDto() {
            when(cityPort.findByName("Warsaw")).thenReturn(Mono.just(cityWarsaw));

            StepVerifier.create(cityService.findByName("Warsaw"))
                    .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Warsaw"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("City not found: CityServiceException thrown with city name")
        void shouldThrowWhenCityNotFound() {
            when(cityPort.findByName("Unknown")).thenReturn(Mono.empty());

            StepVerifier.create(cityService.findByName("Unknown"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CityServiceException.class);
                        assertThat(ex.getMessage()).contains("Unknown");
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Two cities: Flux emits two DTOs")
        void shouldReturnAllCities() {
            City city2 = City.builder().id("city-2").name("Krakow").cinemas(new ArrayList<>()).build();
            when(cityPort.findAll()).thenReturn(Flux.just(cityWarsaw, city2));

            StepVerifier.create(cityService.getAll())
                    .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Warsaw"))
                    .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Krakow"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No cities: Flux completes empty")
        void shouldReturnEmptyFlux() {
            when(cityPort.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(cityService.getAll()).verifyComplete();
        }
    }

    @Nested
    @DisplayName("addCinemaToCity()")
    class AddCinemaToCityTests {

        @Test
        @DisplayName("Happy path: cinema is saved and city is updated")
        void shouldAddCinemaToCitySuccessfully() {
            AddCinemaToCityDto dto = AddCinemaToCityDto.builder()
                    .city("Warsaw")
                    .street("Main St 1")
                    .cinemaHallsCapacity(List.of(
                            CreateCinemaHallDto.builder().rowNo(5).colNo(5).build()
                    ))
                    .build();

            CinemaHall hall = CinemaHall.builder().id("hall-1").positions(new ArrayList<>()).movieEmissions(new ArrayList<>()).build();
            Cinema cinema = Cinema.builder().id("cinema-1").cinemaHalls(new ArrayList<>(List.of(hall))).build();
            City updatedCity = City.builder().id("city-1").name("Warsaw").cinemas(new ArrayList<>(List.of(cinema))).build();

            when(cinemaHallPort.addOrUpdateMany(anyList())).thenReturn(Flux.just(hall));
            when(cinemaPort.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(cityPort.findByName("Warsaw")).thenReturn(Mono.just(cityWarsaw));
            when(cityPort.addOrUpdate(any())).thenReturn(Mono.just(updatedCity));
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cityService.addCinemaToCity(dto))
                    .assertNext(result -> assertThat(result.getName()).isEqualTo("Warsaw"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("City not found: CityServiceException with city name")
        void shouldThrowWhenCityNotFoundDuringAddCinema() {
            AddCinemaToCityDto dto = AddCinemaToCityDto.builder()
                    .city("Atlantis")
                    .cinemaHallsCapacity(List.of(
                            CreateCinemaHallDto.builder().rowNo(3).colNo(3).build()
                    ))
                    .build();

            CinemaHall hall = CinemaHall.builder().id("hall-1").positions(new ArrayList<>()).movieEmissions(new ArrayList<>()).build();
            Cinema cinema = Cinema.builder().id("cinema-1").cinemaHalls(new ArrayList<>(List.of(hall))).build();

            when(cinemaHallPort.addOrUpdateMany(anyList())).thenReturn(Flux.just(hall));
            when(cinemaPort.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(cityPort.findByName("Atlantis")).thenReturn(Mono.empty());
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cityService.addCinemaToCity(dto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CityServiceException.class);
                        assertThat(ex.getMessage()).contains("Atlantis");
                    })
                    .verify();
        }
    }
}
