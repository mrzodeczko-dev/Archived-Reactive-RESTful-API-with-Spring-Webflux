package com.app.infrastructure.routing.handlers;

import com.app.application.dto.CreateTicketOrderDto;
import com.app.application.dto.ResponseErrorDto;
import com.app.application.dto.TicketOrderDto;
import com.app.application.exception.TicketOrderServiceException;
import com.app.application.service.TicketOrderService;
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
public class TicketOrderHandler {

    private final TicketOrderService ticketOrderService;

    @Loggable
    @Operation(
            summary = "POST add ticket order",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateTicketOrderDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TicketOrderDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> addTicketOrder(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateTicketOrderDto.class)
                .switchIfEmpty(Mono.error(() -> new TicketOrderServiceException("Request body is empty")))
                .flatMap(createTicketOrderDto -> ticketOrderService.addTicketOrder(
                        serverRequest.principal(), createTicketOrderDto))
                .flatMap(ticketOrderDto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(ticketOrderDto)));
    }

    @Loggable
    @Operation(
            summary = "GET all ticket orders",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketOrderDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        return ticketOrderService.getAll()
                .collectList()
                .flatMap(ticketOrders -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(ticketOrders)));
    }
}
