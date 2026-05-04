package com.app.infrastructure.routing.handlers;

import com.app.application.dto.ResponseErrorDto;
import com.app.application.exception.AuthenticationException;
import com.app.infrastructure.aspect.annotations.Loggable;
import com.app.infrastructure.security.AppUserDetailsService;
import com.app.infrastructure.security.dto.AuthenticationDto;
import com.app.infrastructure.security.dto.TokensDto;
import com.app.infrastructure.security.tokens.AppTokensService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class LoginHandler {

    private final AppUserDetailsService appUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AppTokensService appTokensService;

    @Loggable
    @Operation(summary = "POST login", requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = AuthenticationDto.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TokensDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> login(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(AuthenticationDto.class)
                .switchIfEmpty(Mono.error(() -> new AuthenticationException("Provide request body")))
                .map(dto -> {
                    if (isNull(dto.getPassword()) || isNull(dto.getUsername())) {
                        throw new AuthenticationException("Provide password and username");
                    }
                    return dto;
                })
                .flatMap(authenticationDto -> appUserDetailsService
                        .findByUsername(authenticationDto.getUsername())
                        .filter(user -> passwordEncoder.matches(authenticationDto.getPassword(), user.getPassword())))
                .switchIfEmpty(Mono.error(() -> new AuthenticationException("Provide valid credentials")))
                .flatMap(appTokensService::generateTokens)
                .flatMap(tokensDto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(tokensDto)));
    }
}
