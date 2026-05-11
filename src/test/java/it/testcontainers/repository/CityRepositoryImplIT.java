package it.testcontainers.repository;

import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.domain.city.City;
import com.rzodeczko.infrastructure.persistence.document.CityDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.CityRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(CityRepositoryImpl.class)
class CityRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private CityPort cityPort;
    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeEach
    void wipe() {
        template.dropCollection(CityDocument.class).block();
    }

    @Test
    @DisplayName("findByName matches the @Query annotation: { 'name': ?0 }")
    void shouldFindByName() {
        cityPort.addOrUpdateMany(List.of(
                City.builder().name("Warsaw").build(),
                City.builder().name("Krakow").build(),
                City.builder().name("Gdansk").build()
        )).blockLast();

        StepVerifier.create(cityPort.findByName("Krakow"))
                .assertNext(city -> assertThat(city.getName()).isEqualTo("Krakow"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByName is exact (case-sensitive); no match → empty Mono")
    void shouldReturnEmptyOnCaseMismatch() {
        cityPort.addOrUpdate(City.builder().name("Warsaw").build()).block();
        StepVerifier.create(cityPort.findByName("warsaw")).verifyComplete();
        StepVerifier.create(cityPort.findByName("WARSAW")).verifyComplete();
    }

    @Test
    @DisplayName("findByName for unknown city → empty Mono")
    void shouldReturnEmptyForUnknownCity() {
        StepVerifier.create(cityPort.findByName("Atlantis")).verifyComplete();
    }

    @Test
    @DisplayName("addOrUpdate persists then findById returns it")
    void shouldRoundTrip() {
        String id = cityPort.addOrUpdate(City.builder().name("Wroclaw").build()).map(City::getId).block();
        assertThat(id).isNotBlank();
        StepVerifier.create(cityPort.findById(id))
                .assertNext(c -> assertThat(c.getName()).isEqualTo("Wroclaw"))
                .verifyComplete();
    }
}