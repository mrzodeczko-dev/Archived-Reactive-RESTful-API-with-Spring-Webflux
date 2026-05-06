package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.TicketPort;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.infrastructure.persistence.mapper.TicketDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoTicketRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TicketRepositoryImpl implements TicketPort {

    private final MongoTicketRepository mongo;

    public TicketRepositoryImpl(MongoTicketRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<Ticket> addOrUpdate(Ticket item) {
        return mongo.save(TicketDocumentMapper.toDocument(item)).map(TicketDocumentMapper::toDomain);
    }

    @Override
    public Flux<Ticket> addOrUpdateMany(List<Ticket> items) {
        return mongo.saveAll(items.stream().map(TicketDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(TicketDocumentMapper::toDomain);
    }

    @Override
    public Flux<Ticket> findAll() {
        return mongo.findAll().map(TicketDocumentMapper::toDomain);
    }

    @Override
    public Mono<Ticket> findById(String id) {
        return mongo.findById(id).map(TicketDocumentMapper::toDomain);
    }

    @Override
    public Flux<Ticket> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(TicketDocumentMapper::toDomain);
    }

    @Override
    public Mono<Ticket> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(TicketDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<Ticket> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(TicketDocumentMapper::toDomain);
    }
}
