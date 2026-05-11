package it.testcontainers.repository;

import com.rzodeczko.application.port.out.TicketPurchasePort;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.infrastructure.persistence.document.TicketPurchaseDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.TicketPurchaseRepositoryImpl;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(TicketPurchaseRepositoryImpl.class)
class TicketPurchaseRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private TicketPurchasePort ticketPurchasePort;
    @Autowired
    private ReactiveMongoTemplate template;

    private User jan;
    private User anna;
    private MovieEmission emissionDramaInHallA;     // movie-1, hall-A
    private MovieEmission emissionThrillerInHallB;  // movie-2, hall-B

    @BeforeEach
    void wipeAndSeed() {
        template.dropCollection(TicketPurchaseDocument.class).block();

        jan = User.builder().username("jan").password("h1").email("jan@example.com").build();
        anna = User.builder().username("anna").password("h2").email("anna@example.com").build();

        Movie drama = Movie.builder().name("Inception").genre("Drama").duration(148)
                .premiereDate(LocalDate.of(2010, 7, 16)).build();
        drama.setId("movie-1");
        Movie thriller = Movie.builder().name("Joker").genre("Thriller").duration(122)
                .premiereDate(LocalDate.of(2019, 10, 4)).build();
        thriller.setId("movie-2");

        emissionDramaInHallA = MovieEmission.builder()
                .movie(drama).cinemaHallId("hall-A")
                .startDateTime(LocalDateTime.of(2026, 5, 10, 18, 0))
                .baseTicketPrice(Money.of("25.00")).isPositionFree(new HashMap<>()).build();
        emissionDramaInHallA.setId("em-1");

        emissionThrillerInHallB = MovieEmission.builder()
                .movie(thriller).cinemaHallId("hall-B")
                .startDateTime(LocalDateTime.of(2026, 6, 1, 20, 0))
                .baseTicketPrice(Money.of("30.00")).isPositionFree(new HashMap<>()).build();
        emissionThrillerInHallB.setId("em-2");

        TicketPurchase janInHallA = TicketPurchase.builder()
                .user(jan).movieEmission(emissionDramaInHallA)
                .purchaseDate(LocalDate.of(2026, 5, 1))
                .tickets(Collections.emptyList())
                .ticketGroupType(TicketGroupType.NORMAL).build();
        TicketPurchase annaInHallA = TicketPurchase.builder()
                .user(anna).movieEmission(emissionDramaInHallA)
                .purchaseDate(LocalDate.of(2026, 5, 5))
                .tickets(Collections.emptyList())
                .ticketGroupType(TicketGroupType.NORMAL).build();
        TicketPurchase janInHallB = TicketPurchase.builder()
                .user(jan).movieEmission(emissionThrillerInHallB)
                .purchaseDate(LocalDate.of(2026, 5, 25))
                .tickets(Collections.emptyList())
                .ticketGroupType(TicketGroupType.NORMAL).build();

        ticketPurchasePort.addOrUpdateMany(List.of(janInHallA, annaInHallA, janInHallB)).blockLast();
    }

    @Nested
    @DisplayName("By user")
    class ByUser {

        @Test
        @DisplayName("findAllByUserUsername returns purchases of given user across emissions")
        void shouldFindAllByUser() {
            StepVerifier.create(ticketPurchasePort.findAllByUserUsername("jan").collectList())
                    .assertNext(l -> {
                        assertThat(l).hasSize(2);
                        assertThat(l).extracting(p -> p.getUser().getUsername())
                                .containsOnly("jan");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByUserUsername of unknown user → empty Flux")
        void shouldReturnEmptyForUnknownUser() {
            StepVerifier.create(ticketPurchasePort.findAllByUserUsername("ghost").collectList())
                    .assertNext(l -> assertThat(l).isEmpty())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("By cinema hall")
    class ByCinemaHall {

        @Test
        @DisplayName("findAllByCinemaHallId — derived query on movieEmission.cinemaHallId")
        void shouldFindByCinemaHall() {
            StepVerifier.create(ticketPurchasePort.findAllByCinemaHallId("hall-A").collectList())
                    .assertNext(l -> assertThat(l).hasSize(2))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByCinemaHallsIds — $in over multiple halls")
        void shouldFindByMultipleHalls() {
            StepVerifier.create(ticketPurchasePort.findAllByCinemaHallsIds(List.of("hall-A", "hall-B"))
                            .collectList())
                    .assertNext(l -> assertThat(l).hasSize(3))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByCinemaHallsIdsAndUsername — $in halls AND user.username")
        void shouldFindByHallsAndUser() {
            StepVerifier.create(ticketPurchasePort.findAllByCinemaHallsIdsAndUsername(
                            List.of("hall-A", "hall-B"), "jan").collectList())
                    .assertNext(l -> assertThat(l).hasSize(2))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("By movie")
    class ByMovie {

        @Test
        @DisplayName("findAllByMovieId — through movieEmission.movie.id")
        void shouldFindByMovieId() {
            StepVerifier.create(ticketPurchasePort.findAllByMovieId("movie-1").collectList())
                    .assertNext(l -> assertThat(l).hasSize(2))
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByMovieIdAndUserUsername — combined nested filter")
        void shouldFindByMovieAndUser() {
            StepVerifier.create(ticketPurchasePort.findAllByMovieIdAndUserUsername("movie-1", "jan")
                            .collectList())
                    .assertNext(l -> assertThat(l).hasSize(1))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("By date")
    class ByDate {

        @Test
        @DisplayName("findAllByPurchaseDateBetween is inclusive")
        void shouldFindByDateRange() {
            StepVerifier.create(ticketPurchasePort.findAllByPurchaseDateBetween(
                            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10)).collectList())
                    .assertNext(l -> assertThat(l).hasSize(2))   // jan 5-1, anna 5-5
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByPurchaseDateAfter — strict greater than")
        void shouldFindAfterDate() {
            StepVerifier.create(ticketPurchasePort.findAllByPurchaseDateAfter(LocalDate.of(2026, 5, 10))
                            .collectList())
                    .assertNext(l -> assertThat(l).hasSize(1))   // jan 5-25
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAllByPurchaseDateBefore — strict less than")
        void shouldFindBeforeDate() {
            StepVerifier.create(ticketPurchasePort.findAllByPurchaseDateBefore(LocalDate.of(2026, 5, 5))
                            .collectList())
                    .assertNext(l -> assertThat(l).hasSize(1))   // jan 5-1 only (anna 5-5 excluded by < strict)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("By emission start time + halls (statistics query)")
    class ByEmissionStartTime {

        @Test
        @DisplayName("findAllByMovieEmissionInDateAndByCinemaHallsIdIn — emission date < given AND hall in list")
        void shouldFindByEmissionStartAndHalls() {
            // hall-A's emission starts on 2026-05-10 18:00, hall-B on 2026-06-01 20:00.
            // Filter: before 2026-05-15 (only hall-A passes) AND hall in {hall-A, hall-B}.
            StepVerifier.create(ticketPurchasePort.findAllByMovieEmissionInDateAndByCinemaHallsIdIn(
                                    LocalDate.of(2026, 5, 15), List.of("hall-A", "hall-B"))
                            .collectList())
                    .assertNext(l -> assertThat(l).hasSize(2))   // both hall-A purchases pass; hall-B's emission is later
                    .verifyComplete();
        }
    }
}