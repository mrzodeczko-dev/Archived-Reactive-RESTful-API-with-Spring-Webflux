package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.TicketPurchaseDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

public interface MongoTicketPurchaseRepository extends ReactiveMongoRepository<TicketPurchaseDocument, String> {

    Flux<TicketPurchaseDocument> findAllByUserUsername(String username);

    Flux<TicketPurchaseDocument> findAllByMovieEmissionCinemaHallIdIn(List<String> cinemaHallsIds);

    @Query("{ 'movieEmission.cinemaHallId': { $in: ?0 }, 'user.username': ?1 }")
    Flux<TicketPurchaseDocument> findAllByMovieEmissionCinemaHallIdInAndUserUsername(List<String> cinemaHallsIds, String username);

    Flux<TicketPurchaseDocument> findAllByPurchaseDateBetween(LocalDate from, LocalDate to);

    Flux<TicketPurchaseDocument> findAllByPurchaseDateAfter(LocalDate from);

    Flux<TicketPurchaseDocument> findAllByPurchaseDateBefore(LocalDate to);

    Flux<TicketPurchaseDocument> findAllByMovieEmissionMovieId(String movieId);

    Flux<TicketPurchaseDocument> findAllByMovieEmissionMovieIdAndUserUsername(String movieId, String username);

    Flux<TicketPurchaseDocument> findAllByMovieEmissionCinemaHallId(String cinemaHallId);

    @Query("{ 'movieEmission.cinemaHallId': { $in: ?1 }, 'movieEmission.startDateTime': { $lt: ?0 } }")
    Flux<TicketPurchaseDocument> findAllByMovieEmissionStartDateTimeBeforeAndMovieEmissionCinemaHallIdIn(
            java.time.LocalDateTime beforeDateTime, List<String> cinemaHallIds);
}
