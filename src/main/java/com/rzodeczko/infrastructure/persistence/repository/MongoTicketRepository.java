package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.TicketDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MongoTicketRepository extends ReactiveMongoRepository<TicketDocument, String> {
}
