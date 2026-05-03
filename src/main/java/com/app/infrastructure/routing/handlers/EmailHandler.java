package com.app.infrastructure.routing.handlers;

import com.app.application.dto.CreateMailDto;
import com.app.application.dto.CreateMailsDto;
import com.app.application.dto.MailDto;
import com.app.application.dto.ResponseErrorDto;
import com.app.application.exception.EmailServiceException;
import com.app.application.service.EmailService;
import com.app.infrastructure.aspect.annotations.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EmailHandler {

    private final EmailService emailService;

    @Loggable
    @Operation(
            summary = "POST send single email",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateMailDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MailDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> sendSingleEmail(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateMailDto.class)
                .switchIfEmpty(Mono.error(() -> new EmailServiceException("No mail info defined")))
                .flatMap(emailService::sendSingleEmail)
                .flatMap(mailDto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(mailDto)))
                .onErrorResume(EmailServiceException.class, e ->
                        ServerResponse
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ResponseErrorDto.builder().message(e.getMessage()).build()));
    }

    @Loggable
    @Operation(
            summary = "POST send multiple emails",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateMailsDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MailDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> sendMultipleEmails(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateMailsDto.class)
                .switchIfEmpty(Mono.error(() -> new EmailServiceException("No mails info defined")))
                .flatMapMany(emailService::sendMultipleEmails)
                .collectList()
                .flatMap(list -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(list)))
                .onErrorResume(EmailServiceException.class, e ->
                        ServerResponse
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ResponseErrorDto.builder().message(e.getMessage()).build()));
    }
}
