package com.app.infrastructure.mongo.initscripts.config;

import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.MongockSpringboot;
import io.mongock.runner.springboot.base.MongockInitializingBeanRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Mongock 5 migration:
 *   - groupId: com.github.cloudyrock.mongock  ->  io.mongock
 *   - driver:  SpringDataMongoV3Driver        ->  SpringDataMongoV4Driver
 *   - builder: MongockSpring5.builder()       ->  MongockSpringboot.builder()
 *   - runner type: SpringInitializingBeanRunner -> MongockInitializingBeanRunner
 *   - addChangeLogsScanPackage()              stays the same
 */
@Configuration
public class MongoMigrationConfiguration {

    @Value("${change-logs-scan-package}")
    private String changeLogsScanPackage;

    @Bean
    public MongockInitializingBeanRunner mongockInitializingBeanRunner(
            ApplicationContext springContext,
            MongoTemplate mongoTemplate) {
        return MongockSpringboot
                .builder()
                .setDriver(SpringDataMongoV4Driver.withDefaultLock(mongoTemplate))
                .addMigrationScanPackage(changeLogsScanPackage)
                .setSpringContext(springContext)
                .setTransactionEnabled(false)
                .buildInitializingBeanRunner();
    }
}
