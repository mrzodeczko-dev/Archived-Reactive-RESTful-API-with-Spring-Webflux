package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.CinemaPort;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.infrastructure.persistence.mapper.CinemaDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoCinemaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CinemaRepositoryImpl implements CinemaPort {

    private final MongoCinemaRepository mongo;

    public CinemaRepositoryImpl(MongoCinemaRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<Cinema> findByCinemaHallId(String id) {
        return mongo.findByCinemaHallId(id).map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Mono<Cinema> addOrUpdate(Cinema cinema) {
        return mongo.save(CinemaDocumentMapper.toDocument(cinema)).map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Flux<Cinema> addOrUpdateMany(List<Cinema> cinemas) {
        return mongo.saveAll(cinemas.stream().map(CinemaDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Flux<Cinema> findAll() {
        return mongo.findAll().map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Mono<Cinema> findById(String id) {
        return mongo.findById(id).map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Flux<Cinema> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Mono<Cinema> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(CinemaDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<Cinema> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(docs -> mongo.deleteAll(docs).then(Mono.just(docs)))
                .flatMapMany(Flux::fromIterable)
                .map(CinemaDocumentMapper::toDomain);
    }

    @Override
    public Flux<Cinema> findAllByCity(String city) {
        return mongo.getAllByCity(city).map(CinemaDocumentMapper::toDomain);
    }
}
