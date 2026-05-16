package com.rzodeczko.infrastructure.security;

import com.rzodeczko.application.exception.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextRepositoryTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private SecurityContextRepository securityContextRepository;

    @Nested
    @DisplayName("load()")
    class LoadTests {

        @Test
        @DisplayName("Bearer header: token stripped and authentication wrapped in security context")
        void shouldLoadSecurityContextFromBearerToken() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "jan@example.com",
                    null,
                    List.of()
            );
            when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(Mono.just(authentication));

            StepVerifier.create(securityContextRepository.load(exchangeWithAuthorization("Bearer jwt-token")))
                    .assertNext(context -> assertThat(context.getAuthentication()).isSameAs(authentication))
                    .verifyComplete();

            ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
            verify(authenticationManager).authenticate(captor.capture());
            assertThat(captor.getValue().getPrincipal()).isEqualTo("");
            assertThat(captor.getValue().getCredentials()).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("Missing Authorization header: empty Mono and no authentication call")
        void shouldCompleteEmptyWhenAuthorizationHeaderIsMissing() {
            StepVerifier.create(securityContextRepository.load(exchangeWithoutAuthorization()))
                    .verifyComplete();

            verifyNoInteractions(authenticationManager);
        }

        @Test
        @DisplayName("Non bearer Authorization header: empty Mono and no authentication call")
        void shouldCompleteEmptyWhenAuthorizationHeaderIsNotBearer() {
            StepVerifier.create(securityContextRepository.load(exchangeWithAuthorization("Basic abc123")))
                    .verifyComplete();

            verifyNoInteractions(authenticationManager);
        }

        @Test
        @DisplayName("Authentication failure: error propagated")
        void shouldPropagateAuthenticationError() {
            when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(Mono.error(new AuthenticationException("bad token")));

            StepVerifier.create(securityContextRepository.load(exchangeWithAuthorization("Bearer broken-token")))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(AuthenticationException.class);
                        assertThat(ex.getMessage()).contains("bad token");
                    })
                    .verify();
        }
    }

    @Test
    @DisplayName("save(): empty Mono")
    void shouldReturnEmptyMonoOnSave() {
        StepVerifier.create(securityContextRepository.save(exchangeWithoutAuthorization(), null))
                .verifyComplete();
    }

    private MockServerWebExchange exchangeWithAuthorization(String authorization) {
        return MockServerWebExchange.from(MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, authorization));
    }

    private MockServerWebExchange exchangeWithoutAuthorization() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/"));
    }
}
