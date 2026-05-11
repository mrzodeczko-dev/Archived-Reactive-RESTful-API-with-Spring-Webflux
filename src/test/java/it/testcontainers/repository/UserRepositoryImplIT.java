package it.testcontainers.repository;

import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.UserRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(UserRepositoryImpl.class)
class UserRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private UserPort userPort;
    @Autowired
    private ReactiveMongoTemplate template;

    private User jan;

    @BeforeEach
    void wipeAndSeed() {
        template.dropCollection(UserDocument.class).block();
        jan = User.builder()
                .username("jan")
                .password("hashed")
                .email("jan@example.com")
                .birthDate(LocalDate.of(1995, 5, 20))
                .build();
    }

    @Test
    @DisplayName("addOrUpdate persists then findByUsername returns it")
    void shouldFindByUsername() {
        userPort.addOrUpdate(jan).block();
        StepVerifier.create(userPort.findByUsername("jan"))
                .assertNext(found -> {
                    assertThat(found.getEmail()).isEqualTo("jan@example.com");
                    assertThat(found.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 20));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByUsername of unknown user → empty Mono")
    void shouldReturnEmptyForUnknownUsername() {
        StepVerifier.create(userPort.findByUsername("ghost")).verifyComplete();
    }

    @Test
    @DisplayName("findByEmail returns the user matching the email")
    void shouldFindByEmail() {
        userPort.addOrUpdate(jan).block();
        StepVerifier.create(userPort.findByEmail("jan@example.com"))
                .assertNext(found -> assertThat(found.getUsername()).isEqualTo("jan"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Role round-trips through Mongo (USER stays USER, ADMIN stays ADMIN)")
    void shouldPreserveRole() {
        // Default User builder yields ROLE_USER.
        User savedUser = userPort.addOrUpdate(jan).block();
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);

        // Promote and re-save.
        savedUser.setRole(Role.ROLE_ADMIN);
        userPort.addOrUpdate(savedUser).block();

        StepVerifier.create(userPort.findByUsername("jan"))
                .assertNext(reloaded -> assertThat(reloaded.getRole()).isEqualTo(Role.ROLE_ADMIN))
                .verifyComplete();
    }

    @Test
    @DisplayName("addOrUpdate on existing id updates rather than duplicating")
    void shouldUpdateNotDuplicate() {
        User saved = userPort.addOrUpdate(jan).block();
        saved.setEmail("jan2@example.com");
        userPort.addOrUpdate(saved).block();
        StepVerifier.create(userPort.findAll().count()).expectNext(1L).verifyComplete();
        StepVerifier.create(userPort.findByEmail("jan2@example.com"))
                .expectNextCount(1).verifyComplete();
    }

    @Test
    @DisplayName("deleteById removes the user")
    void shouldDelete() {
        String id = userPort.addOrUpdate(jan).block().getId();
        userPort.deleteById(id).block();
        StepVerifier.create(userPort.findById(id)).verifyComplete();
    }
}