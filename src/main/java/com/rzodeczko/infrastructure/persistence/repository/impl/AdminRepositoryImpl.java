package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.AdminPort;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.infrastructure.persistence.mapper.UserDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoAdminUserRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AdminRepositoryImpl implements AdminPort {

    private final MongoAdminUserRepository mongo;

    public AdminRepositoryImpl(MongoAdminUserRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<Admin> findById(String id) {
        return mongo.findById(id).map(UserDocumentMapper::toAdminDomain);
    }

    @Override
    public Mono<Admin> findByUsername(String username) {
        return mongo.findByUsername(username).map(UserDocumentMapper::toAdminDomain);
    }

    @Override
    public Mono<Admin> addOrUpdate(Admin item) {
        return mongo.save(UserDocumentMapper.toDocument(item)).map(UserDocumentMapper::toAdminDomain);
    }

    @Override
    public Flux<Admin> addOrUpdateMany(List<Admin> items) {
        return mongo.saveAll(items.stream().map(UserDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(UserDocumentMapper::toAdminDomain);
    }

    @Override
    public Flux<Admin> findAll() {
        return mongo.findAll().map(UserDocumentMapper::toAdminDomain);
    }

    @Override
    public Flux<Admin> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(UserDocumentMapper::toAdminDomain);
    }

    @Override
    public Mono<Admin> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.deleteById(id).then(Mono.just(UserDocumentMapper.toAdminDomain(doc))));
    }

    @Override
    public Flux<Admin> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(UserDocumentMapper::toAdminDomain);
    }
}
