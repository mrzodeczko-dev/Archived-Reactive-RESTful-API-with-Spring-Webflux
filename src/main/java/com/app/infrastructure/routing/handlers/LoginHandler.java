package com.app.infrastructure.routing.handlers;

import com.app.application.dto.ResponseErrorDto;
import com.app.application.exception.LoginServiceException;
import com.app.infrastructure.security.dto.AuthenticationDto;
import com.app.infrastructure.security.dto.TokensDto;
import com.app.infrastructure.security.tokens.AppTokensService;
import com.app.infrastructure.aspect.annotations.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LoginHandler {

    private final ReactiveAuthenticationManager authenticationManager;
    private final AppTokensService appTokensService;

    @Loggable
    @Operation(
            summary = "POST login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TokensDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(AuthenticationDto.class)
                .switchIfEmpty(Mono.error(() -> new LoginServiceException("Request body is empty")))
                .flatMap(authenticationDto -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(
                                authenticationDto.getUsername(),
                                authenticationDto.getPassword())))
                .flatMap(authentication -> appTokensService
                        .generateTokens(org.springframework.security.core.userdetails.User
                                .withUsername(authentication.getName())
                                .password("")
                                .authorities(authentication.getAuthorities())
                                .build()))
                .flatMap(tokens -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(tokens)));
    }
}
