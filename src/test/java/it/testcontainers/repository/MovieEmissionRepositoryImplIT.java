package it.testcontainers.repository;

import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.MovieEmissionRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MovieEmissionRepositoryImpl.class)
class MovieEmissionRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private MovieEmissionPort movieEmissionPort;
    @Autowired
    private ReactiveMongoTemplate template;

    private MovieEmission emissionA;
    private MovieEmission emissionB;
    private MovieEmission emissionC;

    @BeforeEach
    void wipeAndSeed() {
        template.dropCollection(MovieEmissionDocument.class).block();

        Movie movie1 = Movie.builder().name("Inception").genre("Drama").duration(148)
                .premiereDate(LocalDate.of(2010, 7, 16)).build();
        movie1.setId("movie-1");

        Movie movie2 = Movie.builder().name("Joker").genre("Thriller").duration(122)
                .premiereDate(LocalDate.of(2019, 10, 4)).build();
        movie2.setId("movie-2");

        emissionA = MovieEmission.builder()
                .movie(movie1).cinemaHallId("hall-A")
                .startDateTime(LocalDateTime.of(2026, 5, 20, 18, 0))
                .baseTicketPrice(Money.of("25.00")).isPositionFree(new HashMap<>()).build();
        emissionB = MovieEmission.builder()
                .movie(movie1).cinemaHallId("hall-B")
                .startDateTime(LocalDateTime.of(2026, 5, 21, 20, 0))
                .baseTicketPrice(Money.of("25.00")).isPositionFree(new HashMap<>()).build();
        emissionC = MovieEmission.builder()
                .movie(movie2).cinemaHallId("hall-A")
                .startDateTime(LocalDateTime.of(2026, 5, 22, 19, 0))
                .baseTicketPrice(Money.of("30.00")).isPositionFree(new HashMap<>()).build();

        // IMPORTANT: addOrUpdateMany returns NEW instances with generated ids — collect them
        // and reassign the locals so emissionA/B/C have valid ids for later findById() calls
        List<MovieEmission> saved = movieEmissionPort.addOrUpdateMany(List.of(emissionA, emissionB, emissionC)).collectList().block();
        emissionA = saved.get(0);
        emissionB = saved.get(1);
        emissionC = saved.get(2);
    }

    @Test
    @DisplayName("findMovieEmissionsByMovieId returns all emissions of the same movie across halls")
    void shouldFindByMovieId() {
        StepVerifier.create(movieEmissionPort.findMovieEmissionsByMovieId("movie-1").collectList())
                .assertNext(l -> assertThat(l).hasSize(2)
                        .extracting(MovieEmission::getCinemaHallId)
                        .containsExactlyInAnyOrder("hall-A", "hall-B"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findMovieEmissionsByCinemaHallId returns all emissions in that hall across movies")
    void shouldFindByCinemaHallId() {
        StepVerifier.create(movieEmissionPort.findMovieEmissionsByCinemaHallId("hall-A").collectList())
                .assertNext(l -> assertThat(l).hasSize(2)
                        .extracting(e -> e.getMovie().getId())
                        .containsExactlyInAnyOrder("movie-1", "movie-2"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findMovieEmissionsByMovieId on unknown id → empty Flux")
    void shouldReturnEmptyForUnknownMovie() {
        StepVerifier.create(movieEmissionPort.findMovieEmissionsByMovieId("ghost-movie").collectList())
                .assertNext(l -> assertThat(l).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("Money value object round-trips through Mongo via custom converter")
    void shouldRoundTripMoney() {
        StepVerifier.create(movieEmissionPort.findById(emissionC.getId()))
                .assertNext(e -> assertThat(e.getBaseTicketPrice().getValue().toPlainString())
                        .isEqualTo("30.00"))
                .verifyComplete();
    }
}