package it.testcontainers.repository;

import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import com.rzodeczko.infrastructure.persistence.document.CinemaHallDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.CinemaHallRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(CinemaHallRepositoryImpl.class)
class CinemaHallRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private CinemaHallPort cinemaHallPort;
    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeEach
    void wipe() {
        template.dropCollection(CinemaHallDocument.class).block();
    }

    @Test
    @DisplayName("getAllForCinemaById finds halls with matching cinemaId")
    void shouldFindAllForCinema() {
        CinemaHall hall1 = CinemaHall.builder().positions(Collections.emptyList())
                .movieEmissions(Collections.emptyList()).build();
        hall1.setCinemaId("cinema-1");
        CinemaHall hall2 = CinemaHall.builder().positions(Collections.emptyList())
                .movieEmissions(Collections.emptyList()).build();
        hall2.setCinemaId("cinema-1");
        CinemaHall hall3 = CinemaHall.builder().positions(Collections.emptyList())
                .movieEmissions(Collections.emptyList()).build();
        hall3.setCinemaId("cinema-2");

        cinemaHallPort.addOrUpdateMany(List.of(hall1, hall2, hall3)).blockLast();


        StepVerifier.create(cinemaHallPort.getAllForCinemaById("cinema-1").collectList())
                .assertNext(l -> assertThat(l).hasSize(2))
                .verifyComplete();
    }

    @Test
    @DisplayName("getByMovieEmissionId uses $elemMatch on embedded movieEmissions list")
    void shouldFindHallByEmbeddedMovieEmissionId() {
        Movie movie = Movie.builder().name("Inception").genre("Drama").duration(148)
                .premiereDate(LocalDate.of(2010, 7, 16)).build();
        movie.setId("movie-1");

        MovieEmission emission = MovieEmission.builder()
                .movie(movie)
                .startDateTime(LocalDateTime.of(2026, 5, 20, 18, 30))
                .baseTicketPrice(Money.of("25.00"))
                .cinemaHallId("hall-7")
                .isPositionFree(new HashMap<>())
                .build();
        emission.setId("emission-42");

        CinemaHall hall = CinemaHall.builder()
                .positions(List.of(Position.builder().rowNo(1).colNo(1).build()))
                .movieEmissions(List.of(emission))
                .build();
        hall.setCinemaId("cinema-7");

        cinemaHallPort.addOrUpdate(hall).block();

        StepVerifier.create(cinemaHallPort.getByMovieEmissionId("emission-42"))
                .assertNext(found -> {
                    assertThat(found.getCinemaId()).isEqualTo("cinema-7");
                    assertThat(found.getMovieEmissions()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getByMovieEmissionId for non-existing id → empty Mono")
    void shouldReturnEmptyForUnknownEmissionId() {
        StepVerifier.create(cinemaHallPort.getByMovieEmissionId("ghost-emission"))
                .verifyComplete();
    }
}