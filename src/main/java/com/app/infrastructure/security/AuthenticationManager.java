package com.app.infrastructure.security;

import com.app.application.exception.AuthenticationException;
import com.app.domain.security.UserRepository;
import com.app.infrastructure.security.tokens.AppTokensService;
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
    private final UserRepository userRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var token = authentication.getCredentials().toString();

        return Mono.fromCallable(() -> appTokensService.isTokenValid(token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new AuthenticationException("User cannot be authenticated: " + e.getMessage()))
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(() -> new AuthenticationException("AUTH FAILED - TOKEN IS NOT VALID"));
                    }
                    return Mono.fromCallable(() -> appTokensService.getId(token))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(userRepository::findById)
                            .switchIfEmpty(Mono.error(() -> new AuthenticationException("Wrong username")))
                            .map(userFromDb -> (Authentication) new UsernamePasswordAuthenticationToken(
                                    userFromDb.getUsername(),
                                    null,
                                    List.of(new SimpleGrantedAuthority(userFromDb.getRole().toString()))
                            ));
                });
    }
}
