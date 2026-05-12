package it.handlers;

import com.rzodeczko.application.dto.CreateMovieDto;
import com.rzodeczko.application.dto.MovieDto;
import com.rzodeczko.application.exception.MovieServiceException;
import com.rzodeczko.application.service.MovieService;
import com.rzodeczko.presentation.routing.MoviesRouting;
import com.rzodeczko.presentation.routing.userprovider.CurrentUserProvider;
import com.rzodeczko.presentation.routing.handlers.MoviesHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest
@Import({
        MoviesRouting.class,
        MoviesHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class MoviesHandlerSliceTest {

    @Autowired
    ApplicationContext context;

    private WebTestClient client;
    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    private static MovieDto sampleMovie(String id, String name, String genre, int duration) {
        return MovieDto.builder()
                .id(id).name(name).genre(genre).duration(duration)
                .premiereDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("GET /movies/id/{id} → 200 + MovieDto")
    void shouldGetMovieById() {
        when(movieService.getById("m-1")).thenReturn(Mono.just(sampleMovie("m-1", "Inception", "Drama", 148)));

        client.get().uri("/movies/id/m-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("m-1")
                .jsonPath("$.name").isEqualTo("Inception")
                .jsonPath("$.duration").isEqualTo(148);
    }

    @Test
    @DisplayName("GET /movies/id/{id} for unknown id → 404")
    void shouldReturn404ForUnknownMovie() {
        when(movieService.getById("ghost"))
                .thenReturn(Mono.error(new MovieServiceException("No movie with id: ghost")));

        client.get().uri("/movies/id/ghost")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /movies → 200 + JSON array")
    void shouldListAll() {
        when(movieService.getAll()).thenReturn(Flux.just(
                sampleMovie("m-1", "Inception", "Drama", 148),
                sampleMovie("m-2", "Joker", "Thriller", 122)));

        client.get().uri("/movies")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Inception");
    }

    @Test
    @DisplayName("POST /movies → 201 + saved MovieDto")
    void shouldAddMovie() {
        when(movieService.addMovie(any())).thenReturn(Mono.just(sampleMovie("m-3", "New", "Comedy", 95)));

        client.post().uri("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateMovieDto.builder().name("New").genre("Comedy").duration(95)
                        .premiereDate("01-12-2026").build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("m-3");
    }

    @Test
    @DisplayName("DELETE /movies/id/{id} → 200 + deleted MovieDto")
    void shouldDeleteMovie() {
        when(movieService.deleteMovieById("m-1"))
                .thenReturn(Mono.just(sampleMovie("m-1", "Inception", "Drama", 148)));

        client.delete().uri("/movies/id/m-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("m-1");
    }

    @Test
    @DisplayName("GET /movies/filter/genre/{g} → 200 + filtered list")
    void shouldFilterByGenre() {
        when(movieService.getFilteredByGenre("Drama"))
                .thenReturn(Flux.just(sampleMovie("m-1", "Inception", "Drama", 148)));

        client.get().uri("/movies/filter/genre/Drama")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].genre").isEqualTo("Drama");
    }

    @Test
    @DisplayName("PATCH /movies/addToFavorites/{id} requires authenticated principal")
    void shouldAddToFavorites() {
        when(movieService.addMovieToFavorites("m-1", "Jan"))
                .thenReturn(Mono.just(sampleMovie("m-1", "Inception", "Drama", 148)));

        when(currentUserProvider.username()).thenReturn(Mono.just("Jan"));
        client.patch().uri("/movies/addToFavorites/m-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("m-1");
    }

    @Test
    @DisplayName("GET /movies/favorites uses authenticated principal name as user id")
    void shouldListFavorites() {
        when(movieService.getFavoriteMovies("Jan"))
                .thenReturn(Flux.just(sampleMovie("m-1", "Inception", "Drama", 148)));
        when(currentUserProvider.username()).thenReturn(Mono.just("Jan"));

        client
                .get().uri("/movies/favorites")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }
}