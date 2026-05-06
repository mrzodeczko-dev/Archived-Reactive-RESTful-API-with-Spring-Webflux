package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

public interface TicketPurchasePort extends CrudPort<TicketPurchase, String> {

    Flux<TicketPurchase> findAllByUserUsername(String username);

    Flux<TicketPurchase> findAllByCinemaHallsIds(List<String> cinemaHallsIds);

    Flux<TicketPurchase> findAllByCinemaHallsIdsAndUsername(List<String> cinemaHallsIds, String username);

    Flux<TicketPurchase> findAllByPurchaseDateBetween(LocalDate from, LocalDate to);

    Flux<TicketPurchase> findAllByPurchaseDateAfter(LocalDate from);

    Flux<TicketPurchase> findAllByPurchaseDateBefore(LocalDate to);

    Flux<TicketPurchase> findAllByMovieId(String movieId);

    Flux<TicketPurchase> findAllByMovieIdAndUserUsername(String movieId, String username);

    Flux<TicketPurchase> findAllByCinemaHallId(String cinemaHallId);

    Flux<TicketPurchase> findAllByMovieEmissionInDateAndByCinemaHallsIdIn(LocalDate beforeDate, List<String> cinemaHallIds);
}
