package it.testcontainers;

import com.rzodeczko.CinemaApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for repository integration tests backed by a real MongoDB.
 *
 * <p>Spins up a single-node replica set via {@link MongoDBContainer} (production also uses RS,
 * required for Spring Data reactive transactions).
 * <p>Both {@code spring.mongodb.uri} (Boot 4 namespace) and {@code spring.data.mongodb.uri}
 * (legacy / Spring Data) are wired so the test slice picks up the URL regardless of which
 * autoconfig fires.
 *
 * <p>Subclasses must add {@code @DataMongoTest} + {@code @Import(<your-impl>.class)} to load
 * the adapter under test.
 */
@Testcontainers
@ActiveProfiles("testcontainers")
public abstract class AbstractMongoIT {

    @Container
    protected static final MongoDBContainer MONGO;

    static {
        // Wymuszenie API version przed inicjalizacją kontenera
        System.setProperty("api.version", "1.43");
        MONGO = new MongoDBContainer(DockerImageName.parse("mongo:8.3.1"))
                .withReuse(true);
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }
}
