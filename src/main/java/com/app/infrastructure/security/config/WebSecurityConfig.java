package com.app.infrastructure.security.config;

import com.app.application.dto.ErrorMessageDto;
import com.app.application.dto.ResponseDto;
import com.app.infrastructure.security.AuthenticationManager;
import com.app.infrastructure.security.SecurityContextRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

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
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ServerAuthenticationEntryPoint serverAuthenticationEntryPoint() {
        return (serverWebExchange, e) -> {
            // Fixed: was incorrectly returning 500 INTERNAL_SERVER_ERROR.
            // 401 UNAUTHORIZED is the correct status when authentication is missing or invalid.
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
            } catch (JsonProcessingException exception) {
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
                                } catch (JsonProcessingException exception) {
                                    log.error(exception.getMessage(), exception);
                                }
                                return dataBufferFactory.wrap(new byte[]{});
                            }));
        };
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()

                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)

                .exceptionHandling()
                .authenticationEntryPoint(serverAuthenticationEntryPoint())
                .accessDeniedHandler(serverAccessDeniedHandler())

                .and()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/register").permitAll()
                .pathMatchers("/login").permitAll()

                // Fixed: previously permitAll() — enforcing proper role restrictions
                .pathMatchers("/users/**").hasRole("ADMIN")
                .pathMatchers("/statistics/**").permitAll()

                .pathMatchers(HttpMethod.GET, "/cinemas").hasRole("USER")
                .pathMatchers("/cinemas/**").hasRole("ADMIN")

                // Fixed: previously permitAll() — enforcing proper role restrictions
                .pathMatchers("/cities/**").hasRole("USER")

                .pathMatchers(HttpMethod.POST, "/movies/csv").hasRole("ADMIN")
                .pathMatchers("/movies/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/tickets/**").hasRole("USER")
                .pathMatchers("/ticketOrders/**").hasRole("USER")
                .pathMatchers(HttpMethod.POST, "/movieEmissions").hasRole("ADMIN")

                .pathMatchers("/docs/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .pathMatchers("/webjars/swagger-ui/**").permitAll()

                .anyExchange().denyAll()
                .and()
                .build();
    }
}
