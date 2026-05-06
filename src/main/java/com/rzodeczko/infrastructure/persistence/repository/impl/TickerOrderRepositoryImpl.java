package com.rzodeczko.infrastructure.persistence.repository.impl;

import com.rzodeczko.application.port.out.TicketOrderPort;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.infrastructure.persistence.mapper.TicketOrderDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoTicketOrderRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TickerOrderRepositoryImpl implements TicketOrderPort {

    private final MongoTicketOrderRepository mongo;

    public TickerOrderRepositoryImpl(MongoTicketOrderRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mono<TicketOrder> addOrUpdate(TicketOrder item) {
        return mongo.save(TicketOrderDocumentMapper.toDocument(item)).map(TicketOrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketOrder> addOrUpdateMany(List<TicketOrder> items) {
        return mongo.saveAll(items.stream().map(TicketOrderDocumentMapper::toDocument).collect(Collectors.toList()))
                .map(TicketOrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketOrder> findAll() {
        return mongo.findAll().map(TicketOrderDocumentMapper::toDomain);
    }

    @Override
    public Mono<TicketOrder> findById(String id) {
        return mongo.findById(id).map(TicketOrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketOrder> findAllById(List<String> ids) {
        return mongo.findAllById(ids).map(TicketOrderDocumentMapper::toDomain);
    }

    @Override
    public Mono<TicketOrder> deleteById(String id) {
        return mongo.findById(id)
                .flatMap(doc -> mongo.delete(doc).then(Mono.just(TicketOrderDocumentMapper.toDomain(doc))));
    }

    @Override
    public Flux<TicketOrder> deleteAllById(List<String> ids) {
        return mongo.findAllById(ids)
                .collectList()
                .flatMap(list -> mongo.deleteAll(list).then(Mono.just(list)))
                .flatMapMany(Flux::fromIterable)
                .map(TicketOrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<TicketOrder> findAllByUsername(String username) {
        return mongo.findAllByUserUsername(username).map(TicketOrderDocumentMapper::toDomain);
    }
}
