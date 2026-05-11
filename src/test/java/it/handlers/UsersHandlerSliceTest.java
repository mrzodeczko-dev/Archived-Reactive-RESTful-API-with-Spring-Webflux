package it.handlers;

import com.rzodeczko.application.dto.CreateUserDto;
import com.rzodeczko.application.dto.UserDto;
import com.rzodeczko.application.exception.RegistrationUserException;
import com.rzodeczko.application.exception.UserServiceException;
import com.rzodeczko.application.service.UsersService;
import com.rzodeczko.presentation.routing.UsersRouting;
import com.rzodeczko.presentation.routing.handlers.UsersHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        UsersRouting.class,
        UsersHandler.class,
        AbstractHandlerSliceTest.Configs.class})
@ActiveProfiles("handlers")
class UsersHandlerSliceTest {

    @Autowired
    private WebTestClient client;
    @MockitoBean
    private UsersService usersService;


    @Test
    @DisplayName("POST /register with valid body → 201 + UserDto")
    void shouldRegisterSuccessfully() {
        UserDto userDto = UserDto.builder()
                .id("u-1").username("jan").email("jan@example.com").role("ROLE_USER")
                .favoriteMovies(Collections.emptyList()).build();
        when(usersService.register(any(CreateUserDto.class))).thenReturn(Mono.just(userDto));

        client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateUserDto.builder()
                        .username("jan").email("jan@example.com")
                        .password("Secret123!").passwordConfirmation("Secret123!")
                        .birthDate("01-01-1990").build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("u-1")
                .jsonPath("$.username").isEqualTo("jan")
                .jsonPath("$.role").isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("POST /register with invalid data → 400 (validator throws RegistrationUserException)")
    void shouldFailOnValidationError() {
        when(usersService.register(any(CreateUserDto.class)))
                .thenReturn(Mono.error(new RegistrationUserException("password too short")));

        client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateUserDto.builder()
                        .username("a").email("bad").password("x").passwordConfirmation("x")
                        .birthDate("01-01-1990").build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /register with duplicate user → 409")
    void shouldFailOnDuplicate() {
        when(usersService.register(any(CreateUserDto.class)))
                .thenReturn(Mono.error(new RegistrationUserException("User with username: jan already exists")));

        client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateUserDto.builder()
                        .username("jan").email("jan@example.com")
                        .password("Secret123!").passwordConfirmation("Secret123!")
                        .birthDate("01-01-1990").build())
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("POST /register with empty body → 400")
    void shouldFailOnEmptyBody() {
        client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /users → 200 + JSON array")
    void shouldListAllUsers() {
        UserDto u1 = UserDto.builder().id("1").username("jan").role("ROLE_USER")
                .favoriteMovies(Collections.emptyList()).build();
        UserDto u2 = UserDto.builder().id("2").username("anna").role("ROLE_ADMIN")
                .favoriteMovies(Collections.emptyList()).build();
        when(usersService.getAll()).thenReturn(Flux.just(u1, u2));

        client.get().uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].username").isEqualTo("jan")
                .jsonPath("$[1].username").isEqualTo("anna");
    }

    @Test
    @DisplayName("GET /users/username/{u} for unknown user → 404")
    void shouldReturn404ForUnknownUser() {
        when(usersService.getByUsername("ghost"))
                .thenReturn(Mono.error(new UserServiceException("No user with username: ghost")));

        client.get().uri("/users/username/ghost")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}