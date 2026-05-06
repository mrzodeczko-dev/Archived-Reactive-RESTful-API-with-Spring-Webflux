package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.infrastructure.persistence.mapper.MovieEmissionDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoMovieEmissionRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Repository
public class MovieEmissionRepositoryImpl implements MovieEmissionPort {

    private final MongoMovieEmissionRepository mongo;

    public MovieEmissionRepositoryImpl(MongoMovieEmissionRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<MovieEmission> addOrUpdate(MovieEmission item) {
        return mongo.save(MovieEmissionDocumentMapper.toDocument(item)).map(MovieEmissionDocumentMapper::toDomain);
    }

    @Override
    public Flux<MovieEmission> addOrUpdateMany(List<MovieEmission> items) {
        return mongo.saveAll(items.stream().map(MovieEmissionDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(MovieEmissionDocumentMapper::toDomain);
    }

    @Override
    public Flux<MovieEmission> findAll() {
        return mongo.findAll().map(MovieEmissionDocumentMapper::toDomain);
    }

    @Override
    public Mono<MovieEmission> findById(String id) {
        return nonNull(id) ? mongo.findById(id).map(MovieEmissionDocumentMapper::toDomain) : Mono.empty();
    }

    @Override
    public Flux<MovieEmission> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(MovieEmissionDocumentMapper::toDomain);
    }

    @Override
    public Mono<MovieEmission> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(MovieEmissionDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<MovieEmission> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(MovieEmissionDocumentMapper::toDomain);
    }

    @Override
    public Flux<MovieEmission> findMovieEmissionsByMovieId(String movieId) {
        return mongo.findMovieEmissionsByMovieId(movieId).map(MovieEmissionDocumentMapper::toDomain);
    }

    @Override
    public Flux<MovieEmission> findMovieEmissionsByCinemaHallId(String cinemaHallId) {
        return mongo.findMovieEmissionsByCinemaHallId(cinemaHallId).map(MovieEmissionDocumentMapper::toDomain);
    }
}
