package com.rzodeczko.infrastructure.security;

import com.rzodeczko.application.exception.AuthenticationException;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.infrastructure.security.tokens.AppTokensService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final AppTokensService appTokensService;
    private final UserPort userPort;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var credentials = authentication.getCredentials();
        if (credentials == null) {
            return Mono.error(() -> new AuthenticationException("Credentials not provided"));
        }
        var token = credentials.toString();

        return Mono.fromCallable(() -> appTokensService.isTokenValid(token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new AuthenticationException("User cannot be authenticated: " + e.getMessage()))
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(() -> new AuthenticationException("AUTH FAILED - TOKEN IS NOT VALID"));
                    }
                    return Mono.fromCallable(() -> appTokensService.getId(token))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(userPort::findById)
                            .switchIfEmpty(Mono.error(() -> new AuthenticationException("Wrong username")))
                            .map(userFromDb -> (Authentication) new UsernamePasswordAuthenticationToken(
                                    userFromDb.getUsername(),
                                    null,
                                    List.of(new SimpleGrantedAuthority(userFromDb.getRole().toString()))
                            ));
                });
    }
}
