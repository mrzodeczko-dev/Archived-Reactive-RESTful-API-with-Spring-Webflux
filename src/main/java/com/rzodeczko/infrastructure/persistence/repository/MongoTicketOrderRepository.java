package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.TicketOrderDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MongoTicketOrderRepository extends ReactiveMongoRepository<TicketOrderDocument, String> {

    Flux<TicketOrderDocument> findAllByUserUsername(String username);
}
