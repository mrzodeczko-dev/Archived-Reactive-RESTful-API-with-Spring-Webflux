package com.rzodeczko.presentation.routing.handlers;

import com.rzodeczko.application.dto.CreateMailsDto;
import com.rzodeczko.application.dto.MailDto;
import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.application.dto.SendEmailToSelfDto;
import com.rzodeczko.application.exception.EmailServiceException;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.service.EmailService;
import com.rzodeczko.infrastructure.aspect.annotations.Loggable;
import com.rzodeczko.presentation.routing.userprovider.CurrentUserProvider;
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
    private final UserPort userPort;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Sends an email to the currently authenticated user. The request body contains only
     * {@code title} and {@code htmlContent} — the recipient is resolved server-side from the
     * JWT principal so the endpoint cannot be used as an arbitrary mail relay.
     */
    @Loggable
    @Operation(
            summary = "POST send email to logged user",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = SendEmailToSelfDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MailDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Mail send failure", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> sendSingleEmail(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SendEmailToSelfDto.class)
                .switchIfEmpty(Mono.error(() -> new EmailServiceException("No mail info defined")))
                .zipWith(currentUserProvider.username())
                .flatMap(tuple -> userPort.findByUsername(tuple.getT2())
                        .switchIfEmpty(Mono.error(() -> new EmailServiceException("Logged user not found")))
                        .flatMap(user -> emailService.sendEmailToSelf(tuple.getT1(), user.getEmail())))
                .flatMap(mailDto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(mailDto)));
    }

    /**
     * Bulk emails. Requires ADMIN role (configured in {@code WebSecurityConfig}). Recipients
     * supplied in the body are sent verbatim — the admin is responsible for the addresses.
     */
    @Loggable
    @Operation(
            summary = "POST send multiple emails (admin only)",
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
                        .body(BodyInserters.fromValue(list)));
    }
}