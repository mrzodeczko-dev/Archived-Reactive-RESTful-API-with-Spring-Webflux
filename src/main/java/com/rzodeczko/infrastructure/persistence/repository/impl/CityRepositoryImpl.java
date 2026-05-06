package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.domain.city.City;
import com.rzodeczko.infrastructure.persistence.mapper.CityDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoCityRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CityRepositoryImpl implements CityPort {

    private final MongoCityRepository mongo;

    public CityRepositoryImpl(MongoCityRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<City> findByName(String name) {
        return mongo.findByName(name).map(CityDocumentMapper::toDomain);
    }

    @Override
    public Mono<City> addOrUpdate(City item) {
        return mongo.save(CityDocumentMapper.toDocument(item)).map(CityDocumentMapper::toDomain);
    }

    @Override
    public Flux<City> addOrUpdateMany(List<City> items) {
        return mongo.saveAll(items.stream().map(CityDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(CityDocumentMapper::toDomain);
    }

    @Override
    public Flux<City> findAll() {
        return mongo.findAll().map(CityDocumentMapper::toDomain);
    }

    @Override
    public Mono<City> findById(String id) {
        return mongo.findById(id).map(CityDocumentMapper::toDomain);
    }

    @Override
    public Flux<City> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(CityDocumentMapper::toDomain);
    }

    @Override
    public Mono<City> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(CityDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<City> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(CityDocumentMapper::toDomain);
    }
}
