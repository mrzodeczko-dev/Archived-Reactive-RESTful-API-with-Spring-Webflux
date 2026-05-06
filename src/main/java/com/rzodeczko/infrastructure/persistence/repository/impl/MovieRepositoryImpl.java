package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.infrastructure.persistence.mapper.MovieDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoMovieRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MovieRepositoryImpl implements MoviePort {

    private final MongoMovieRepository mongo;

    public MovieRepositoryImpl(MongoMovieRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<Movie> addOrUpdate(Movie movie) {
        return mongo.save(MovieDocumentMapper.toDocument(movie)).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> addOrUpdateMany(List<Movie> movies) {
        return mongo.saveAll(movies.stream().map(MovieDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAll() {
        return mongo.findAll().map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Mono<Movie> findById(String id) {
        return mongo.findById(id).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Mono<Movie> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(MovieDocumentMapper.toDomain(doc))));
    }

    @Override
    public Mono<Movie> findByNameAndGenre(String name, String genre) {
        return mongo.findByNameAndGenre(name, genre).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByGenre(String genre) {
        return mongo.findAllByGenre(genre).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByName(String name) {
        return mongo.findAllByName(name).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByDurationBetween(int min, int max) {
        return mongo.findAllByDurationBetween(min, max).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByDurationGreaterThanEqual(int min) {
        return mongo.findAllByDurationGreaterThanEqual(min).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByDurationLessThanEqual(int max) {
        return mongo.findAllByDurationLessThanEqual(max).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByPremiereDateBetween(LocalDate from, LocalDate to) {
        return mongo.findAllByPremiereDateBetween(from, to).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByPremiereDateGreaterThanEqual(LocalDate from) {
        return mongo.findAllByPremiereDateGreaterThanEqual(from).map(MovieDocumentMapper::toDomain);
    }

    @Override
    public Flux<Movie> findAllByPremiereDateLessThanEqual(LocalDate to) {
        return mongo.findAllByPremiereDateLessThanEqual(to).map(MovieDocumentMapper::toDomain);
    }
}
