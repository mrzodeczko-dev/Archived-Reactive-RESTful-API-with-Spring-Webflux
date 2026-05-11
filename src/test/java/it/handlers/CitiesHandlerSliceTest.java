package it.handlers;

import com.rzodeczko.CinemaApplication;
import com.rzodeczko.application.dto.AddCinemaToCityDto;
import com.rzodeczko.application.dto.CityDto;
import com.rzodeczko.application.dto.CreateCityDto;
import com.rzodeczko.application.exception.CityServiceException;
import com.rzodeczko.application.service.CityService;
import com.rzodeczko.presentation.routing.BaseJsonRouter;
import com.rzodeczko.presentation.routing.CitiesRouting;
import com.rzodeczko.presentation.routing.handlers.CitiesHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        CitiesRouting.class,
        CitiesHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class CitiesHandlerSliceTest {

    @Autowired
    private WebTestClient client;
    @MockitoBean
    private CityService cityService;

    @Test
    @DisplayName("POST /cities with body Mono<CreateCityDto> → 201 + saved CityDto")
    void shouldCreateCity() {
        CityDto saved = CityDto.builder().id("c-1").name("Warsaw").cinemas(Collections.emptyList()).build();
        when(cityService.addCity(any(Mono.class))).thenReturn(Mono.just(saved));

        client.post().uri("/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateCityDto.builder().name("Warsaw").build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("c-1")
                .jsonPath("$.name").isEqualTo("Warsaw");
    }

    @Test
    @DisplayName("GET /cities → 200 + list of cities")
    void shouldListAllCities() {
        when(cityService.getAll()).thenReturn(Flux.just(
                CityDto.builder().id("1").name("Warsaw").cinemas(Collections.emptyList()).build(),
                CityDto.builder().id("2").name("Krakow").cinemas(Collections.emptyList()).build()));

        client.get().uri("/cities")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Warsaw")
                .jsonPath("$[1].name").isEqualTo("Krakow");
    }

    @Test
    @DisplayName("GET /cities/name/{name} for known city → 200")
    void shouldFindByName() {
        when(cityService.findByName("Warsaw")).thenReturn(Mono.just(
                CityDto.builder().id("1").name("Warsaw").cinemas(Collections.emptyList()).build()));

        client.get().uri("/cities/name/Warsaw")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Warsaw");
    }

    @Test
    @DisplayName("GET /cities/name/{name} for unknown city → 404")
    void shouldReturn404ForUnknownCity() {
        when(cityService.findByName("Atlantis"))
                .thenReturn(Mono.error(new CityServiceException("No city with name: Atlantis")));

        client.get().uri("/cities/name/Atlantis")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /cities (addCinemaToCity) → 200 + updated CityDto")
    void shouldAddCinemaToCity() {
        CityDto updated = CityDto.builder().id("1").name("Warsaw").cinemas(Collections.emptyList()).build();
        when(cityService.addCinemaToCity(any(AddCinemaToCityDto.class))).thenReturn(Mono.just(updated));

        client.put().uri("/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(AddCinemaToCityDto.builder().city("Warsaw").build())
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}