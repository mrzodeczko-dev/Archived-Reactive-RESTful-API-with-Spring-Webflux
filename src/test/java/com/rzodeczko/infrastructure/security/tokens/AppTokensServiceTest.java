package com.rzodeczko.infrastructure.security.tokens;

import com.rzodeczko.application.exception.AuthenticationException;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.infrastructure.security.dto.TokensDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppTokensServiceTest {

    @Mock
    private UserPort userPort;

    private AppTokensService appTokensService;
    private SecretKey secretKey;
    private JwtProperties jwtProperties;
    private User userFromDb;
    private org.springframework.security.core.userdetails.User securityUser;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor("12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8));
        jwtProperties = new JwtProperties(
                new JwtProperties.AccessToken(60_000),
                new JwtProperties.RefreshToken(120_000, "accessTokenExpiration"),
                new JwtProperties.Token("Bearer ")
        );
        appTokensService = new AppTokensService(jwtProperties, secretKey, userPort);
        userFromDb = User.builder()
                .id("user-id-1")
                .username("jan@example.com")
                .password("hashed-pass")
                .role(Role.ROLE_USER)
                .build();
        securityUser = new org.springframework.security.core.userdetails.User(
                "jan@example.com",
                "hashed-pass",
                List.of()
        );
    }

    @Nested
    @DisplayName("generateTokens()")
    class GenerateTokensTests {

        @Test
        @DisplayName("Happy path: returns access and refresh token for user id")
        void shouldGenerateTokensForExistingUser() {
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.just(userFromDb));

            StepVerifier.create(appTokensService.generateTokens(securityUser))
                    .assertNext(tokens -> {
                        assertThat(tokens.getAccessToken()).isNotBlank();
                        assertThat(tokens.getRefreshToken()).isNotBlank();
                        assertThat(appTokensService.getId(tokens.getAccessToken())).isEqualTo("user-id-1");
                        assertThat(appTokensService.getId(tokens.getRefreshToken())).isEqualTo("user-id-1");
                        assertThat(appTokensService.isTokenValid(tokens.getAccessToken())).isTrue();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Null user: SecurityException emitted, no DB call")
        void shouldErrorWhenUserIsNull() {
            StepVerifier.create(appTokensService.generateTokens(null))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(SecurityException.class);
                        assertThat(ex.getMessage()).contains("user is null");
                    })
                    .verify();

            verifyNoInteractions(userPort);
        }

        @Test
        @DisplayName("User not found: SecurityException with username")
        void shouldErrorWhenUserNotFound() {
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.empty());

            StepVerifier.create(appTokensService.generateTokens(securityUser))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(SecurityException.class);
                        assertThat(ex.getMessage()).contains("jan@example.com");
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("refreshTokens()")
    class RefreshTokensTests {

        @Test
        @DisplayName("Happy path: valid refresh token produces new tokens")
        void shouldRefreshTokens() {
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.just(userFromDb));
            when(userPort.findById("user-id-1")).thenReturn(Mono.just(userFromDb));

            TokensDto initialTokens = appTokensService.generateTokens(securityUser).block();

            StepVerifier.create(appTokensService.refreshTokens(initialTokens.getRefreshToken()))
                    .assertNext(tokens -> {
                        assertThat(tokens.getAccessToken()).isNotBlank();
                        assertThat(tokens.getRefreshToken()).isNotBlank();
                        assertThat(appTokensService.getId(tokens.getAccessToken())).isEqualTo("user-id-1");
                    })
                    .verifyComplete();

            verify(userPort).findById("user-id-1");
        }

        @Test
        @DisplayName("Access token provided: AuthenticationException emitted")
        void shouldRejectAccessTokenAsRefreshToken() {
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.just(userFromDb));

            TokensDto initialTokens = appTokensService.generateTokens(securityUser).block();

            StepVerifier.create(appTokensService.refreshTokens(initialTokens.getAccessToken()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("not a refresh token");
                    })
                    .verify();
        }

        @Test
        @DisplayName("User missing during refresh: AuthenticationException emitted")
        void shouldErrorWhenUserMissingDuringRefresh() {
            String refreshToken = buildToken(
                    "missing-user-id",
                    new Date(System.currentTimeMillis() + 120_000),
                    Map.of("accessTokenExpiration", System.currentTimeMillis() + 60_000)
            );
            when(userPort.findById("missing-user-id")).thenReturn(Mono.empty());

            StepVerifier.create(appTokensService.refreshTokens(refreshToken))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("User not found");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Malformed token: AuthenticationException emitted")
        void shouldErrorWhenRefreshTokenIsMalformed() {
            StepVerifier.create(appTokensService.refreshTokens("not-a-jwt"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("Invalid refresh token");
                    })
                    .verify();
        }
    }

    private String buildToken(String subject, Date expiration, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiration)
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }
}
