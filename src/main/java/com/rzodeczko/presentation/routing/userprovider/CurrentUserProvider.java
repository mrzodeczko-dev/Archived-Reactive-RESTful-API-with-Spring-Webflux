package com.rzodeczko.presentation.routing.userprovider;

import com.rzodeczko.application.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    public Mono<Authentication> authentication() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(auth -> auth.isAuthenticated()
                        && !(auth instanceof AnonymousAuthenticationToken))
                .switchIfEmpty(Mono.error(
                        new AuthenticationException("User cannot be found in security context")));
    }

    public Mono<String> username() {
        return authentication().map(Authentication::getName);
    }
}
