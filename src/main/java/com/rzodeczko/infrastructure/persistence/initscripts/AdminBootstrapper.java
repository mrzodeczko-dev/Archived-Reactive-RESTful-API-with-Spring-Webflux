package com.rzodeczko.infrastructure.persistence.initscripts;

import com.rzodeczko.application.port.out.AdminPort;
import com.rzodeczko.application.port.out.PasswordEncoderPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.domain.security.BaseUser;
import com.rzodeczko.domain.security.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Replaces the Mongock-based {@code InitScripts} for environments where Mongock
 * autoconfig is not available (e.g. Spring Boot 4). Idempotent — runs on every
 * startup but only creates the admin if it doesn't already exist; if a user with
 * the admin username exists with USER role, it is promoted to ADMIN.
 *
 * <p>Blocks startup until the bootstrap finishes (max 30 s) so the app never
 * starts accepting requests before the admin is in place.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapper implements ApplicationRunner {

    private static final Duration BOOTSTRAP_TIMEOUT = Duration.ofSeconds(30);

    private final UserPort userPort;
    private final AdminPort adminPort;
    private final PasswordEncoderPort passwordEncoder;
    private final InitParams initParams;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Bootstrapping admin user '{}'", initParams.getAdminUsername());

        Mono<? extends BaseUser> result = userPort
                .findByUsername(initParams.getAdminUsername())
                .flatMap(existing -> {
                    if (existing.getRole() == Role.ROLE_ADMIN) {
                        log.info("Admin '{}' already present — skipping", existing.getUsername());
                        return Mono.empty();
                    }
                    log.info("User '{}' exists with role {} — promoting to ADMIN",
                            existing.getUsername(), existing.getRole());
                    existing.setRole(Role.ROLE_ADMIN);
                    return userPort.addOrUpdate(existing).cast(BaseUser.class);
                })
                .switchIfEmpty(adminPort
                        .addOrUpdate(new Admin(
                                initParams.getAdminUsername(),
                                passwordEncoder.encode(initParams.getPassword())))
                        .doOnNext(a -> log.info("Created bootstrap admin '{}'", a.getUsername()))
                        .cast(BaseUser.class));

        try {
            result.block(BOOTSTRAP_TIMEOUT);
        } catch (RuntimeException e) {
            log.error("Admin bootstrap failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}