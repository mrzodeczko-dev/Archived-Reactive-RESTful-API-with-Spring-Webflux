package it.testcontainers.repository;

import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.MovieRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MovieRepositoryImpl.class)
class MovieRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private MoviePort moviePort;
    @Autowired
    private ReactiveMongoTemplate template;

    private Movie inception;
    private Movie pulpFiction;
    private Movie joker;
    private Movie hangover;

    @BeforeEach
    void wipeAndSeed() {
        template.dropCollection(MovieDocument.class).block();
        inception = Movie.builder().name("Inception").genre("Drama").duration(148)
                .premiereDate(LocalDate.of(2010, 7, 16)).build();
        pulpFiction = Movie.builder().name("Pulp Fiction").genre("Drama").duration(154)
                .premiereDate(LocalDate.of(1994, 10, 14)).build();
        joker = Movie.builder().name("Joker").genre("Thriller").duration(122)
                .premiereDate(LocalDate.of(2019, 10, 4)).build();
        hangover = Movie.builder().name("The Hangover").genre("Comedy").duration(100)
                .premiereDate(LocalDate.of(2009, 6, 5)).build();
    }

    @Nested
    @DisplayName("CRUD round-trip")
    class Crud {

        @Test
        @DisplayName("addOrUpdate generates id; findById returns the same movie")
        void shouldRoundTrip() {
            String id = moviePort.addOrUpdate(inception).map(Movie::getId).block();
            assertThat(id).isNotBlank();
            StepVerifier.create(moviePort.findById(id))
                    .assertNext(found -> {
                        assertThat(found.getName()).isEqualTo("Inception");
                        assertThat(found.getDuration()).isEqualTo(148);
                        assertThat(found.getPremiereDate()).isEqualTo(LocalDate.of(2010, 7, 16));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("addOrUpdate on existing id updates instead of duplicating")
        void shouldUpdateNotDuplicate() {
            Movie saved = moviePort.addOrUpdate(inception).block();
            saved.setName("Inception (Director's Cut)");
            moviePort.addOrUpdate(saved).block();
            StepVerifier.create(moviePort.findAll().count())
                    .expectNext(1L).verifyComplete();
        }

        @Test
        @DisplayName("addOrUpdateMany inserts all in one go")
        void shouldBulkInsert() {
            StepVerifier.create(moviePort.addOrUpdateMany(List.of(inception, pulpFiction, joker, hangover)))
                    .expectNextCount(4).verifyComplete();
            StepVerifier.create(moviePort.findAll().count())
                    .expectNext(4L).verifyComplete();
        }

        @Test
        @DisplayName("deleteById removes the doc and returns the deleted entity")
        void shouldDelete() {
            String id = moviePort.addOrUpdate(joker).block().getId();
            StepVerifier.create(moviePort.deleteById(id))
                    .assertNext(d -> assertThat(d.getName()).isEqualTo("Joker"))
                    .verifyComplete();
            StepVerifier.create(moviePort.findById(id)).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Custom finders")
    class Finders {

        @BeforeEach
        void seed() {
            moviePort.addOrUpdateMany(List.of(inception, pulpFiction, joker, hangover)).blockLast();
        }

        @Test
        @DisplayName("findByNameAndGenre returns the unique match")
        void shouldFindByNameAndGenre() {
            StepVerifier.create(moviePort.findByNameAndGenre("Inception", "Drama"))
                    .assertNext(m -> assertThat(m.getDuration()).isEqualTo(148))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findByNameAndGenre — name match but wrong genre → empty")
        void shouldReturnEmptyOnGenreMismatch() {
            StepVerifier.create(moviePort.findByNameAndGenre("Inception", "Comedy")).verifyComplete();
        }

        @Test
        @DisplayName("findAllByGenre returns matches, case-sensitive")
        void shouldFindAllByGenre() {
            StepVerifier.create(moviePort.findAllByGenre("Drama").collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactlyInAnyOrder("Inception", "Pulp Fiction"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByName returns exact-name matches")
        void shouldFindAllByName() {
            StepVerifier.create(moviePort.findAllByName("Joker").collectList())
                    .assertNext(l -> assertThat(l).hasSize(1))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByDurationBetween is inclusive on both bounds")
        void shouldFindByDurationRange() {
            StepVerifier.create(moviePort.findAllByDurationBetween(100, 130).collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactlyInAnyOrder("Joker", "The Hangover"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByDurationGreaterThanEqual lower-bound only")
        void shouldFindByDurationGte() {
            StepVerifier.create(moviePort.findAllByDurationGreaterThanEqual(150).collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactly("Pulp Fiction"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByDurationLessThanEqual upper-bound only")
        void shouldFindByDurationLte() {
            StepVerifier.create(moviePort.findAllByDurationLessThanEqual(110).collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactly("The Hangover"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByPremiereDateBetween filters by date inclusive")
        void shouldFindByPremiereRange() {
            StepVerifier.create(moviePort.findAllByPremiereDateBetween(
                            LocalDate.of(2009, 1, 1), LocalDate.of(2019, 12, 31)).collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactlyInAnyOrder("The Hangover", "Inception", "Joker"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByPremiereDateGreaterThanEqual filters lower bound only")
        void shouldFindByPremiereGte() {
            StepVerifier.create(moviePort.findAllByPremiereDateGreaterThanEqual(LocalDate.of(2015, 1, 1))
                            .collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactly("Joker"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByPremiereDateLessThanEqual filters upper bound only")
        void shouldFindByPremiereLte() {
            StepVerifier.create(moviePort.findAllByPremiereDateLessThanEqual(LocalDate.of(2000, 1, 1))
                            .collectList())
                    .assertNext(l -> assertThat(l).extracting(Movie::getName)
                            .containsExactly("Pulp Fiction"))
                    .verifyComplete();
        }
    }
}
