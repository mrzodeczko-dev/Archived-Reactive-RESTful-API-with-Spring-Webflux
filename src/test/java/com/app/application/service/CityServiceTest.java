package com.app.application.service;

import com.app.application.dto.AddCinemaToCityDto;
import com.app.application.dto.CityDto;
import com.app.application.dto.CreateCinemaHallDto;
import com.app.application.dto.CreateCityDto;
import com.app.application.exception.CityServiceException;
import com.app.domain.cinema.Cinema;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.City;
import com.app.domain.city.CityRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private CinemaHallRepository cinemaHallRepository;
    @Mock
    private TransactionalOperator transactionalOperator;

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
            when(cityRepository.addOrUpdate(any())).thenReturn(Mono.just(cityWarsaw));

            StepVerifier.create(cityService.addCity(Mono.just(dto)))
                    .assertNext(result -> assertThat(result.getName()).isEqualTo("Warsaw"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Repository error: exception propagated from addOrUpdate")
        void shouldPropagateRepositoryError() {
            CreateCityDto dto = CreateCityDto.builder().name("Warsaw").build();
            when(cityRepository.addOrUpdate(any()))
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
            when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(cityWarsaw));

            StepVerifier.create(cityService.findByName("Warsaw"))
                    .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Warsaw"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("City not found: CityServiceException thrown with city name")
        void shouldThrowWhenCityNotFound() {
            when(cityRepository.findByName("Unknown")).thenReturn(Mono.empty());

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
            when(cityRepository.findAll()).thenReturn(Flux.just(cityWarsaw, city2));

            StepVerifier.create(cityService.getAll())
                    .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Warsaw"))
                    .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Krakow"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No cities: Flux completes empty")
        void shouldReturnEmptyFlux() {
            when(cityRepository.findAll()).thenReturn(Flux.empty());

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

            when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(hall));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(cityRepository.findByName("Warsaw")).thenReturn(Mono.just(cityWarsaw));
            when(cityRepository.addOrUpdate(any())).thenReturn(Mono.just(updatedCity));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

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

            when(cinemaHallRepository.addOrUpdateMany(anyList())).thenReturn(Flux.just(hall));
            when(cinemaRepository.addOrUpdate(any())).thenReturn(Mono.just(cinema));
            when(cityRepository.findByName("Atlantis")).thenReturn(Mono.empty());
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(cityService.addCinemaToCity(dto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(CityServiceException.class);
                        assertThat(ex.getMessage()).contains("Atlantis");
                    })
                    .verify();
        }
    }
}
