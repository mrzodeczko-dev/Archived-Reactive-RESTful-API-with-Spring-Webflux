package it.handlers;

import com.rzodeczko.application.port.out.PasswordEncoderPort;
import com.rzodeczko.infrastructure.security.AppUserDetailsService;
import com.rzodeczko.infrastructure.security.dto.TokensDto;
import com.rzodeczko.infrastructure.security.tokens.AppTokensService;
import com.rzodeczko.presentation.routing.LoginRouting;
import com.rzodeczko.presentation.routing.handlers.LoginHandler;
import com.rzodeczko.presentation.routing.handlers.RefreshHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Slice test for {@link LoginHandler} routed via {@link LoginRouting}.
 *
 * <p>Loads only the WebFlux infrastructure + the route + handler. Service-layer
 * collaborators (DB, password encoder, JWT service) are mocked via
 * {@code @MockitoBean}. Security is replaced with a no-op so tests focus on
 * routing + handler logic, not authorization.
 */
@WebFluxTest
@Import({
        LoginRouting.class,
        LoginHandler.class,
        RefreshHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class LoginHandlerSliceTest {

    @Autowired
    private WebTestClient client;
    @MockitoBean
    private AppUserDetailsService userDetailsService;
    @MockitoBean
    private PasswordEncoderPort passwordEncoder;
    @MockitoBean
    private AppTokensService tokensService;

    @Test
    @DisplayName("POST /login with valid credentials → 200 + access/refresh tokens")
    void shouldLoginSuccessfully() {
        User springUser = new User("jan", "hashed", true, true, true, true, Collections.emptyList());
        TokensDto tokens = TokensDto.builder().accessToken("acc-123").refreshToken("ref-456").build();

        when(userDetailsService.findByUsername("jan")).thenReturn(Mono.just(springUser));
        when(passwordEncoder.matches("Secret123!", "hashed")).thenReturn(true);
        when(tokensService.generateTokens(any(User.class))).thenReturn(Mono.just(tokens));

        client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.rzodeczko.infrastructure.security.dto.AuthenticationDto("jan", "Secret123!"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("acc-123")
                .jsonPath("$.refreshToken").isEqualTo("ref-456");
    }

    @Test
    @DisplayName("POST /login with empty body → 401 (handler propagates AuthenticationException)")
    void shouldFailWithEmptyBody() {
        client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /login with wrong password → 401")
    void shouldFailOnWrongPassword() {
        User springUser = new User("jan", "hashed", true, true, true, true, Collections.emptyList());

        when(userDetailsService.findByUsername("jan")).thenReturn(Mono.just(springUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.rzodeczko.infrastructure.security.dto.AuthenticationDto("jan", "Wrong!"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /login with unknown user → 401")
    void shouldFailOnUnknownUser() {
        when(userDetailsService.findByUsername("ghost")).thenReturn(Mono.empty());

        client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.rzodeczko.infrastructure.security.dto.AuthenticationDto("ghost", "Whatever123!"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}