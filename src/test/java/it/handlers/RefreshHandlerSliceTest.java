package it.handlers;

import com.rzodeczko.application.exception.AuthenticationException;
import com.rzodeczko.application.port.out.PasswordEncoderPort;
import com.rzodeczko.infrastructure.security.AppUserDetailsService;
import com.rzodeczko.infrastructure.security.dto.RefreshTokenDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
        LoginRouting.class,
        LoginHandler.class,
        RefreshHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class RefreshHandlerSliceTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private AppTokensService tokensService;

    // Required by LoginHandler (also present in the loaded context via LoginRouting)
    @MockitoBean
    private AppUserDetailsService userDetailsService;
    @MockitoBean
    private PasswordEncoderPort passwordEncoder;

    @Test
    @DisplayName("POST /refresh with valid refresh token → 200 + new access/refresh tokens")
    void shouldReturnNewTokensForValidRefreshToken() {
        TokensDto newTokens = TokensDto.builder()
                .accessToken("new-acc-token")
                .refreshToken("new-ref-token")
                .build();

        when(tokensService.refreshTokens("valid.refresh.token")).thenReturn(Mono.just(newTokens));

        client.post().uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshTokenDto("valid.refresh.token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("new-acc-token")
                .jsonPath("$.refreshToken").isEqualTo("new-ref-token");
    }

    @Test
    @DisplayName("POST /refresh with missing refreshToken field → 401")
    void shouldReturn401WhenRefreshTokenFieldAbsent() {
        client.post().uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /refresh with blank refreshToken → 401")
    void shouldReturn401WhenRefreshTokenBlank() {
        client.post().uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshTokenDto(""))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /refresh with expired token → 401")
    void shouldReturn401WhenRefreshTokenExpired() {
        when(tokensService.refreshTokens(anyString()))
                .thenReturn(Mono.error(new AuthenticationException("Refresh token has expired")));

        client.post().uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshTokenDto("expired.refresh.token"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error.message").isEqualTo("Refresh token has expired");
    }

    @Test
    @DisplayName("POST /refresh with access token instead of refresh token → 401")
    void shouldReturn401WhenAccessTokenGivenInsteadOfRefreshToken() {
        when(tokensService.refreshTokens(anyString()))
                .thenReturn(Mono.error(new AuthenticationException("Provided token is not a refresh token")));

        client.post().uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshTokenDto("access.token.given.by.mistake"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error.message").isEqualTo("Provided token is not a refresh token");
    }

    @Test
    @DisplayName("POST /refresh with tampered/invalid token → 401")
    void shouldReturn401WhenRefreshTokenInvalid() {
        when(tokensService.refreshTokens(anyString()))
                .thenReturn(Mono.error(new AuthenticationException("Invalid refresh token")));

        client.post().uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshTokenDto("tampered.jwt.token"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
