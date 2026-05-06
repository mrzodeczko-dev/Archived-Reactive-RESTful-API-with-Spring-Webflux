package com.rzodeczko.infrastructure.persistence.config;

import com.mongodb.ReadPreference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@RequiredArgsConstructor
public class ReactiveMongoConfig {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory mongoDatabaseFactory) {
        return new ReactiveMongoTransactionManager(mongoDatabaseFactory);
    }

    @PostConstruct
    public void init() {
        reactiveMongoTemplate.setReadPreference(ReadPreference.primaryPreferred());
    }
}
