package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.infrastructure.persistence.mapper.CinemaHallDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoCinemaHallRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CinemaHallRepositoryImpl implements CinemaHallPort {

    private final MongoCinemaHallRepository mongo;

    public CinemaHallRepositoryImpl(MongoCinemaHallRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<CinemaHall> addOrUpdate(CinemaHall item) {
        return mongo.save(CinemaHallDocumentMapper.toDocument(item)).map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Flux<CinemaHall> addOrUpdateMany(List<CinemaHall> items) {
        return mongo.saveAll(items.stream().map(CinemaHallDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Flux<CinemaHall> findAll() {
        return mongo.findAll().map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Mono<CinemaHall> findById(String id) {
        return mongo.findById(id).map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Flux<CinemaHall> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Mono<CinemaHall> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(CinemaHallDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<CinemaHall> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Flux<CinemaHall> getAllForCinemaById(String cinemaId) {
        return mongo.findByCinemaId(cinemaId).map(CinemaHallDocumentMapper::toDomain);
    }

    @Override
    public Mono<CinemaHall> getByMovieEmissionId(String movieEmissionId) {
        return mongo.findByMovieEmissionId(movieEmissionId).map(CinemaHallDocumentMapper::toDomain);
    }
}
