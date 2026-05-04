package com.app.infrastructure.repository.mongo;

import com.app.domain.ticket_purchase.TicketPurchase;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

public interface MongoTicketPurchaseRepository extends ReactiveMongoRepository<TicketPurchase, String> {

    Flux<TicketPurchase> findAllByUserUsername(String username);

    Flux<TicketPurchase> findAllByMovieEmissionCinemaHallIdIn(List<String> cinemaHallsIds);

    // Fixed: first param must be a single String matched with $in via @Query,
    // because Spring Data derives 'CinemaHallId' as scalar String, not List.
    @Query("{ 'movieEmission.cinemaHallId': { $in: ?0 }, 'user.username': ?1 }")
    Flux<TicketPurchase> findAllByMovieEmissionCinemaHallIdInAndUserUsername(List<String> cinemaHallsIds, String username);

    Flux<TicketPurchase> findAllByPurchaseDateBetween(LocalDate from, LocalDate to);

    Flux<TicketPurchase> findAllByPurchaseDateAfter(LocalDate from);

    Flux<TicketPurchase> findAllByPurchaseDateBefore(LocalDate to);

    Flux<TicketPurchase> findAllByMovieEmissionMovieId(String movieId);

    Flux<TicketPurchase> findAllByMovieEmissionMovieIdAndUserUsername(String movieId, String username);

    Flux<TicketPurchase> findAllByMovieEmissionCinemaHallId(String cinemaHallId);

    // Fixed: movieEmission is an embedded object (not array), so $elemMatch is wrong.
    // Correct query uses dot-notation on the embedded fields directly.
    @Query("{ 'movieEmission.cinemaHallId': { $in: ?1 }, 'movieEmission.startDateTime': { $lt: ?0 } }")
    Flux<TicketPurchase> findAllByMovieEmissionStartDateTimeBeforeAndMovieEmissionCinemaHallIdIn(
            java.time.LocalDateTime beforeDateTime, List<String> cinemaHallIds);
}
