package com.rzodeczko.infrastructure.security.config;

import com.rzodeczko.application.dto.ErrorMessageDto;
import com.rzodeczko.application.dto.ResponseDto;
import com.rzodeczko.infrastructure.security.AuthenticationManager;
import com.rzodeczko.infrastructure.security.SecurityContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public ServerAuthenticationEntryPoint serverAuthenticationEntryPoint() {
        return (serverWebExchange, e) -> {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return serverWebExchange
                        .getResponse()
                        .writeWith(Mono.just(dataBufferFactory.wrap(
                                objectMapper.writeValueAsBytes(
                                        ResponseDto.builder()
                                                .error(ErrorMessageDto.builder()
                                                        .message(e.getMessage())
                                                        .build())
                                                .build()))));
            } catch (JacksonException exception) {
                log.error(exception.getMessage(), exception);
            }
            return serverWebExchange
                    .getResponse()
                    .writeWith(Mono.just(dataBufferFactory.wrap(new byte[]{})));
        };
    }

    @Bean
    public ServerAccessDeniedHandler serverAccessDeniedHandler() {
        return (serverWebExchange, e) -> {
            serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return serverWebExchange
                    .getResponse()
                    .writeWith(serverWebExchange.getPrincipal()
                            .map(principal -> {
                                try {
                                    return dataBufferFactory
                                            .wrap(objectMapper.writeValueAsBytes(ResponseDto
                                                    .builder()
                                                    .error(ErrorMessageDto.builder()
                                                            .message("%s for username: %s"
                                                                    .formatted(e.getMessage(), principal.getName()))
                                                            .build())
                                                    .build()));
                                } catch (JacksonException exception) {
                                    log.error(exception.getMessage(), exception);
                                }
                                return dataBufferFactory.wrap(new byte[]{});
                            }));
        };
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

                        .pathMatchers("/users/**").hasRole("ADMIN")
                        .pathMatchers("/emails/**").hasRole("USER")
                        .pathMatchers("/statistics/**").permitAll()

                        .pathMatchers(HttpMethod.GET, "/cinemas").hasRole("USER")
                        .pathMatchers("/cinemas/**").hasRole("ADMIN")
                        .pathMatchers("/cities/**").hasRole("USER")

                        .pathMatchers(HttpMethod.POST, "/movies/csv").hasRole("ADMIN")
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

                        .anyExchange().denyAll()
                )
                .build();
    }
}
