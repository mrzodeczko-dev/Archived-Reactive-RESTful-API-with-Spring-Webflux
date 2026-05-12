package it.handlers;

import com.rzodeczko.application.dto.AverageTicketPriceByCityDto;
import com.rzodeczko.application.dto.CityFrequencyDto;
import com.rzodeczko.application.dto.MostPopularMovieGroupedByCityDto;
import com.rzodeczko.application.dto.MovieFrequencyByGenreDto;
import com.rzodeczko.application.dto.MovieFrequencyDto;
import com.rzodeczko.application.service.StatisticsService;
import com.rzodeczko.presentation.routing.StatisticsRouting;
import com.rzodeczko.presentation.routing.handlers.StatisticsHandler;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        StatisticsRouting.class,
        StatisticsHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class StatisticsHandlerSliceTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("GET /statistics/cities/cinemaFrequency → 200 + city frequency list")
    void shouldGetCinemaFrequencyByCity() {
        when(statisticsService.findCitiesFrequency()).thenReturn(Flux.just(
                CityFrequencyDto.builder().city("Warsaw").frequency(3).build(),
                CityFrequencyDto.builder().city("Krakow").frequency(2).build()));

        client.get().uri("/statistics/cities/cinemaFrequency")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].city").isEqualTo("Warsaw")
                .jsonPath("$[0].frequency").isEqualTo(3);
    }

    @Test
    @DisplayName("GET /statistics/cities/cinemaFrequency/max → 200 + city with max frequency")
    void shouldGetCityWithMaxFrequency() {
        when(statisticsService.findCitiesWithMostFrequency()).thenReturn(Flux.just(
                CityFrequencyDto.builder().city("Warsaw").frequency(5).build()));

        client.get().uri("/statistics/cities/cinemaFrequency/max")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].city").isEqualTo("Warsaw")
                .jsonPath("$[0].frequency").isEqualTo(5);
    }

    @Test
    @DisplayName("GET /statistics/movies/mostPopular/byCity → 200 + most popular movies grouped by city")
    void shouldGetMostPopularMovieGroupedByCity() {
        when(statisticsService.findMostPopularMovieGroupedByCity()).thenReturn(Flux.just(
                MostPopularMovieGroupedByCityDto.builder()
                        .city("Warsaw")
                        .movieFrequency(List.of(movieFrequency("Inception", 10)))
                        .build()));

        client.get().uri("/statistics/movies/mostPopular/byCity")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].city").isEqualTo("Warsaw")
                .jsonPath("$[0].movieFrequency[0].movie.name").isEqualTo("Inception");
    }

    @Test
    @DisplayName("GET /statistics/movies/frequency → 200 + movie frequency list")
    void shouldGetAllMoviesFrequency() {
        when(statisticsService.findAllMoviesFrequency()).thenReturn(Flux.just(
                movieFrequency("Inception", 15),
                movieFrequency("Joker", 10)));

        client.get().uri("/statistics/movies/frequency")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].movie.name").isEqualTo("Inception")
                .jsonPath("$[0].frequency").isEqualTo(15);
    }

    @Test
    @DisplayName("GET /statistics/movies/mostPopularGroupedByGenre/byCity/{city} → 200 + genre frequency")
    void shouldGetMostPopularMoviesGroupedByGenreInCity() {
        when(statisticsService.findMostPopularMoviesGroupedByGenreInCity("Warsaw")).thenReturn(Flux.just(
                MovieFrequencyByGenreDto.builder().genre("Drama").frequency(8).build()));

        client.get().uri("/statistics/movies/mostPopularGroupedByGenre/byCity/Warsaw")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].genre").isEqualTo("Drama");
    }

    @Test
    @DisplayName("GET /statistics/averageTicketPrice → 200 + average price by city")
    void shouldGetAverageTicketPriceGroupedByCity() {
        when(statisticsService.getAverageTicketPriceGroupedByCity()).thenReturn(Flux.just(
                AverageTicketPriceByCityDto.builder().city("Warsaw").averageTicketPrice(BigDecimal.valueOf(29.99)).build()));

        client.get().uri("/statistics/averageTicketPrice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].city").isEqualTo("Warsaw");
    }

    @Test
    @DisplayName("GET /statistics/cities/cinemaFrequency → 200 + empty list when no data")
    void shouldReturnEmptyWhenNoStatistics() {
        when(statisticsService.findCitiesFrequency()).thenReturn(Flux.empty());

        client.get().uri("/statistics/cities/cinemaFrequency")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    private static MovieFrequencyDto movieFrequency(String movieName, Integer frequency) {
        return MovieFrequencyDto.builder()
                .movie(com.rzodeczko.application.dto.MovieDto.builder().name(movieName).build())
                .frequency(frequency)
                .build();
    }
}
