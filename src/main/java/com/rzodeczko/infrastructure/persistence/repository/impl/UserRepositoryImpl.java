package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.infrastructure.persistence.mapper.UserDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoUserRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserPort {

    private final MongoUserRepository mongo;

    public UserRepositoryImpl(MongoUserRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return mongo.findByUsername(username).map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Mono<User> addOrUpdate(User user) {
        return mongo.save(UserDocumentMapper.toDocument(user)).map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Flux<User> addOrUpdateMany(List<User> users) {
        return mongo.saveAll(users.stream().map(UserDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Flux<User> findAll() {
        return mongo.findAll().map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Mono<User> findById(String id) {
        return mongo.findById(id).map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Flux<User> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Mono<User> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.deleteById(id).then(Mono.just(UserDocumentMapper.toUserDomain(doc))));
    }

    @Override
    public Flux<User> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(UserDocumentMapper::toUserDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return mongo.findByEmail(email).map(UserDocumentMapper::toUserDomain);
    }
}
