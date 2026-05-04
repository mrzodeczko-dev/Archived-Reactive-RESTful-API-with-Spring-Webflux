package com.app.infrastructure.routing.handlers;

import com.app.application.dto.CreateTicketOrderDto;
import com.app.application.dto.ResponseErrorDto;
import com.app.application.dto.TicketOrderDto;
import com.app.application.service.TicketOrderService;
import com.app.infrastructure.aspect.annotations.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
            summary = "POST order ticket",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateTicketOrderDto.class), mediaType = "application/json")),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TicketOrderDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> orderTickets(final ServerRequest serverRequest) {

        return serverRequest.bodyToMono(CreateTicketOrderDto.class)
                .flatMap(value -> ticketOrderService.addTicketOrder(serverRequest.principal(), value))
                .flatMap(value -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(value)));
    }

    @Loggable
    @Operation(
            summary = "PUT cancel order",
            parameters = {@Parameter(name = "orderId", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TicketOrderDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> cancelOrder(ServerRequest serverRequest) {

        return serverRequest.principal()
                .flatMap(principal -> ticketOrderService.cancelOrder(principal.getName(), serverRequest.pathVariable("orderId")))
                .flatMap(ticketOrder -> ServerResponse
                        .status(200)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(ticketOrder))
                );

    }

    @Loggable
    @Operation(
            summary = "GET all ticket orders by username",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketOrderDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAllTicketOrdersByUsername(ServerRequest serverRequest) {

        return serverRequest.principal()
                .flatMapMany(principal -> ticketOrderService.getAllTicketOrdersForLoggedUser(principal.getName()))
                .collectList()
                .flatMap(list -> ServerResponse
                        .status(200)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(list))
                );
    }
}
