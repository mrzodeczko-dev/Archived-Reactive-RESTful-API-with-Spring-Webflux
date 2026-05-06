package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.TicketPurchasePort;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.infrastructure.persistence.mapper.TicketPurchaseDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoTicketPurchaseRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TicketPurchaseRepositoryImpl implements TicketPurchasePort {

    private final MongoTicketPurchaseRepository mongo;

    public TicketPurchaseRepositoryImpl(MongoTicketPurchaseRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<TicketPurchase> addOrUpdate(TicketPurchase item) {
        return mongo.save(TicketPurchaseDocumentMapper.toDocument(item)).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> addOrUpdateMany(List<TicketPurchase> items) {
        return mongo.saveAll(items.stream().map(TicketPurchaseDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAll() {
        return mongo.findAll().map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Mono<TicketPurchase> findById(String id) {
        return mongo.findById(id).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Mono<TicketPurchase> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(TicketPurchaseDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<TicketPurchase> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByUserUsername(String username) {
        return mongo.findAllByUserUsername(username).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByCinemaHallsIds(List<String> cinemaHallsIds) {
        return mongo.findAllByMovieEmissionCinemaHallIdIn(cinemaHallsIds).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByCinemaHallsIdsAndUsername(List<String> cinemaHallsIds, String username) {
        return mongo.findAllByMovieEmissionCinemaHallIdInAndUserUsername(cinemaHallsIds, username)
                .map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByPurchaseDateBetween(LocalDate from, LocalDate to) {
        return mongo.findAllByPurchaseDateBetween(from, to).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByPurchaseDateAfter(LocalDate from) {
        return mongo.findAllByPurchaseDateAfter(from).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByPurchaseDateBefore(LocalDate to) {
        return mongo.findAllByPurchaseDateBefore(to).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByMovieId(String movieId) {
        return mongo.findAllByMovieEmissionMovieId(movieId).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByMovieIdAndUserUsername(String movieId, String username) {
        return mongo.findAllByMovieEmissionMovieIdAndUserUsername(movieId, username)
                .map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByCinemaHallId(String cinemaHallId) {
        return mongo.findAllByMovieEmissionCinemaHallId(cinemaHallId).map(TicketPurchaseDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketPurchase> findAllByMovieEmissionInDateAndByCinemaHallsIdIn(
            LocalDate beforeDate, List<String> cinemaHallIds) {
        return mongo.findAllByMovieEmissionStartDateTimeBeforeAndMovieEmissionCinemaHallIdIn(
                beforeDate.atTime(0, 0), cinemaHallIds).map(TicketPurchaseDocumentMapper::toDomain);
    }
}
