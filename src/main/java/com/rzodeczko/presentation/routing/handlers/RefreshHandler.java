package com.rzodeczko.presentation.routing.handlers;

import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.application.exception.AuthenticationException;
import com.rzodeczko.infrastructure.aspect.annotations.Loggable;
import com.rzodeczko.infrastructure.security.dto.RefreshTokenDto;
import com.rzodeczko.infrastructure.security.dto.TokensDto;
import com.rzodeczko.infrastructure.security.tokens.AppTokensService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RefreshHandler {

    private final AppTokensService appTokensService;

    @Loggable
    @Operation(summary = "POST refresh", requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = RefreshTokenDto.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New tokens issued", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TokensDto.class))
            }),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Missing request body", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> refresh(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RefreshTokenDto.class)
                .switchIfEmpty(Mono.error(() -> new AuthenticationException("Provide request body")))
                .flatMap(dto -> {
                    if (!StringUtils.hasText(dto.getRefreshToken())) {
                        return Mono.error(() -> new AuthenticationException("Provide refresh token"));
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> appTokensService.refreshTokens(dto.getRefreshToken()))
                .flatMap(tokensDto -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(tokensDto)));
    }
}
