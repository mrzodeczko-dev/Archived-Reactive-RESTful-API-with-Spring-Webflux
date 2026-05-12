package it.handlers;

import com.rzodeczko.application.dto.CinemaDto;
import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.exception.CinemaServiceException;
import com.rzodeczko.application.service.CinemaService;
import com.rzodeczko.presentation.routing.CinemasRouting;
import com.rzodeczko.presentation.routing.handlers.CinemasHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        CinemasRouting.class,
        CinemasHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class CinemasHandlerSliceTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private CinemaService cinemaService;

    private static CinemaDto sampleCinema(String id, String city) {
        return CinemaDto.builder()
                .id(id)
                .city(city)
                .street("Main St 1")
                .hallsCapacity(Map.of("h-1", 200))
                .build();
    }

    @Test
    @DisplayName("POST /cinemas -> 201 + saved CinemaDto")
    void shouldCreateCinema() {
        CinemaDto saved = sampleCinema("c-1", "CinemaCity");
        when(cinemaService.addCinema(any())).thenReturn(Mono.just(saved));

        client.post().uri("/cinemas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateCinemaDto.builder()
                        .city("CinemaCity")
                        .street("Main St 1")
                        .cinemaHallsCapacity(List.of(CreateCinemaHallDto.builder().rowNo(10).colNo(20).build()))
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("c-1")
                .jsonPath("$.city").isEqualTo("CinemaCity")
                .jsonPath("$.street").isEqualTo("Main St 1");
    }

    @Test
    @DisplayName("GET /cinemas -> 200 + list of cinemas")
    void shouldListAllCinemas() {
        when(cinemaService.getAll()).thenReturn(Flux.just(
                sampleCinema("c-1", "CinemaCity"),
                sampleCinema("c-2", "Helios")));

        client.get().uri("/cinemas")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].city").isEqualTo("CinemaCity")
                .jsonPath("$[1].city").isEqualTo("Helios");
    }

    @Test
    @DisplayName("GET /cinemas/city/{city} -> 200 + filtered list")
    void shouldGetCinemasByCity() {
        when(cinemaService.getAllByCity("Warsaw")).thenReturn(Flux.just(
                sampleCinema("c-1", "CinemaCity")));

        client.get().uri("/cinemas/city/Warsaw")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].city").isEqualTo("CinemaCity");
    }

    @Test
    @DisplayName("GET /cinemas/city/{city} with no match -> 200 + empty array")
    void shouldReturnEmptyForUnknownCity() {
        when(cinemaService.getAllByCity("Atlantis")).thenReturn(Flux.empty());

        client.get().uri("/cinemas/city/Atlantis")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("PUT /cinemas/id/{id}/addCinemaHall -> 200 + updated CinemaDto")
    void shouldAddCinemaHall() {
        CinemaDto updated = CinemaDto.builder()
                .id("c-1")
                .city("CinemaCity")
                .street("Main St 1")
                .hallsCapacity(Map.of("h-1", 200))
                .build();
        when(cinemaService.addCinemaHallToCinema(eq("c-1"), any(CreateCinemaHallDto.class)))
                .thenReturn(Mono.just(updated));

        client.put().uri("/cinemas/id/c-1/addCinemaHall")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateCinemaHallDto.builder().rowNo(10).colNo(20).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("c-1")
                .jsonPath("$.hallsCapacity.h-1").isEqualTo(200);
    }

    @Test
    @DisplayName("PUT /cinemas/id/{id}/addCinemaHall for unknown cinema -> 500")
    void shouldReturn5xxForUnknownCinema() {
        when(cinemaService.addCinemaHallToCinema(eq("unknown"), any(CreateCinemaHallDto.class)))
                .thenReturn(Mono.error(new CinemaServiceException("Cinema not found")));

        client.put().uri("/cinemas/id/unknown/addCinemaHall")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateCinemaHallDto.builder().rowNo(10).colNo(20).build())
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
