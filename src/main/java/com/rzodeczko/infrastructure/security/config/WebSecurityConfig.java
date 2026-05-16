package com.rzodeczko.infrastructure.security.config;

import com.rzodeczko.application.dto.ErrorMessageDto;
import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.infrastructure.security.AuthenticationManager;
import com.rzodeczko.infrastructure.security.SecurityContextRepository;
import com.rzodeczko.infrastructure.web.RequestIdWebFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final SecurityContextRepository securityContextRepository;
    private final DataBufferFactory dataBufferFactory;

    /**
     * 401 Unauthorized handler. Logs the real reason server-side with request id;
     * returns a generic message to the client to avoid leaking auth internals.
     */
    @Bean
    public ServerAuthenticationEntryPoint serverAuthenticationEntryPoint() {
        return (exchange, e) -> {
            String reqId = RequestIdWebFilter.get(exchange);
            log.warn("Authentication failed [reqId={}] on {} {}: {}",
                    reqId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    e.getMessage());

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(buildBody("Authentication required", reqId)));
        };
    }


    @Bean
    public ServerAccessDeniedHandler serverAccessDeniedHandler() {
        return (exchange, e) -> {
            String reqId = RequestIdWebFilter.get(exchange);

            Mono<Void> auditLogMono = exchange.getPrincipal()
                    .doOnNext(principal -> log.warn(
                            "Access denied [reqId={}] user='{}' on {} {}: {}",
                            reqId,
                            principal.getName(),
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            e.getMessage()))
                    .switchIfEmpty(Mono.fromRunnable(() -> log.warn(
                            "Access denied [reqId={}] anonymous on {} {}: {}",
                            reqId,
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            e.getMessage())))
                    .onErrorResume(err -> {
                        log.warn("Failed to retrieve principal for access denied audit [reqId={}]: {}", reqId, err.getMessage());
                        return Mono.empty();
                    })
                    .then();

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer body = buildBody("Access denied", reqId);

            return auditLogMono.then(exchange.getResponse().writeWith(Mono.just(body)));
        };
    }

    private DataBuffer buildBody(String clientMessage, String requestId) {
        try {
            return dataBufferFactory.wrap(objectMapper.writeValueAsBytes(
                    ResponseErrorDto.builder()
                            .error(ErrorMessageDto.builder().message(clientMessage).build())
                            .requestId(requestId)
                            .build()));
        } catch (JacksonException ex) {
            log.error("Failed to serialize security error body [reqId={}]", requestId, ex);
            return dataBufferFactory.wrap(new byte[]{});
        }
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(serverAuthenticationEntryPoint())
                        .accessDeniedHandler(serverAccessDeniedHandler())
                )

                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/register").permitAll()
                        .pathMatchers("/login").permitAll()
                        .pathMatchers("/refresh").permitAll()

                        .pathMatchers("/users/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/emails/send/multiple").hasRole("ADMIN")
                        .pathMatchers("/emails/send/single").hasAnyRole("USER", "ADMIN")

                        .pathMatchers("/statistics/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/cities/csv").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/cinemas/csv").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/cinemaHalls/cinemaId/{cinemaId}/csv").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/movies/csv").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/movieEmissions/csv").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/cinemas").hasRole("USER")
                        .pathMatchers("/cinemas/**").hasRole("ADMIN")
                        .pathMatchers("/cities/**").hasRole("USER")

                        .pathMatchers("/movies/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/tickets/**").hasRole("USER")
                        .pathMatchers("/ticketOrders/**").hasRole("USER")
                        .pathMatchers("/ticketsOrders/**").hasRole("USER")
                        .pathMatchers(HttpMethod.POST, "/movieEmissions").hasRole("ADMIN")
                        .pathMatchers("/movieEmissions/**").hasAnyRole("USER", "ADMIN")

                        .pathMatchers("/admin/ticketPurchases/**").hasRole("ADMIN")
                        .pathMatchers("/ticketPurchases/**").hasRole("USER")

                        .pathMatchers("/docs/**").permitAll()
                        .pathMatchers("/docs").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/v3/api-docs").permitAll()
                        .pathMatchers("/webjars/swagger-ui/**").permitAll()

                        .pathMatchers("/actuator/health").permitAll()

                        .anyExchange().denyAll()
                )
                .build();
    }
}
