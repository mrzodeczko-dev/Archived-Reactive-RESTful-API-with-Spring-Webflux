package it.testcontainers.repository;

import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.infrastructure.persistence.document.CinemaDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.CinemaRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(CinemaRepositoryImpl.class)
class CinemaRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private CinemaPort cinemaPort;
    @Autowired
    private ReactiveMongoTemplate template;

    private Cinema cinemaA;     // city=Warsaw, has 2 halls
    private Cinema cinemaB;     // city=Warsaw, has 1 hall
    private Cinema cinemaC;     // city=Krakow, has 0 halls

    @BeforeEach
    void wipeAndSeed() {
        template.dropCollection(CinemaDocument.class).block();

        // Cinema halls with explicit ids — must be unique to test the $elemMatch query.
        CinemaHall hallA1 = CinemaHall.builder().positions(Collections.emptyList())
                .movieEmissions(Collections.emptyList()).build();
        hallA1.setId("hall-a1");
        CinemaHall hallA2 = CinemaHall.builder().positions(Collections.emptyList())
                .movieEmissions(Collections.emptyList()).build();
        hallA2.setId("hall-a2");
        CinemaHall hallB1 = CinemaHall.builder().positions(Collections.emptyList())
                .movieEmissions(Collections.emptyList()).build();
        hallB1.setId("hall-b1");

        cinemaA = Cinema.builder().street("Main St 1").cinemaHalls(List.of(hallA1, hallA2)).build();
        cinemaA.setCity("Warsaw");
        cinemaB = Cinema.builder().street("Other St 5").cinemaHalls(List.of(hallB1)).build();
        cinemaB.setCity("Warsaw");
        cinemaC = Cinema.builder().street("Floriańska 10").cinemaHalls(Collections.emptyList()).build();
        cinemaC.setCity("Krakow");

        cinemaPort.addOrUpdateMany(List.of(cinemaA, cinemaB, cinemaC)).blockLast();
    }

    @Test
    @DisplayName("findByCinemaHallId uses $elemMatch on embedded cinemaHalls — finds the parent cinema")
    void shouldFindCinemaByEmbeddedCinemaHallId() {
        StepVerifier.create(cinemaPort.findByCinemaHallId("hall-a2"))
                .assertNext(c -> {
                    assertThat(c.getStreet()).isEqualTo("Main St 1");
                    assertThat(c.getCinemaHalls()).extracting(CinemaHall::getId)
                            .containsExactlyInAnyOrder("hall-a1", "hall-a2");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByCinemaHallId for non-existing hall id → empty Mono")
    void shouldReturnEmptyForUnknownHall() {
        StepVerifier.create(cinemaPort.findByCinemaHallId("hall-XYZ-does-not-exist"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllByCity returns all cinemas with matching city")
    void shouldFindAllByCity() {
        StepVerifier.create(cinemaPort.findAllByCity("Warsaw").collectList())
                .assertNext(l -> assertThat(l).extracting(Cinema::getStreet)
                        .containsExactlyInAnyOrder("Main St 1", "Other St 5"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllByCity for unknown city → empty Flux")
    void shouldReturnEmptyForUnknownCity() {
        StepVerifier.create(cinemaPort.findAllByCity("Atlantis").collectList())
                .assertNext(l -> assertThat(l).isEmpty())
                .verifyComplete();
    }
}