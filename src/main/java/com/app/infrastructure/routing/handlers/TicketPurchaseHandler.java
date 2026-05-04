package com.app.infrastructure.routing.handlers;

import com.app.application.dto.CreateTicketPurchaseDto;
import com.app.application.dto.ResponseErrorDto;
import com.app.application.dto.TicketPurchaseDto;
import com.app.application.service.TicketPurchaseService;
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
public class TicketPurchaseHandler {

    private final TicketPurchaseService ticketPurchaseService;

    @Loggable
    @Operation(
            summary = "POST add ticket purchase from order",
            parameters = @Parameter(in = ParameterIn.PATH, name = "ticketOrderId"),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TicketPurchaseDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> purchaseTicketFromOrder(ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMap(principal -> ticketPurchaseService.purchaseTicketFromOrder(principal.getName(), serverRequest.pathVariable("ticketOrderId")))
                .flatMap(ticketPurchase -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(ticketPurchase))
                );
    }

    @Loggable
    @Operation(
            summary = "POST add ticket purchase",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateTicketPurchaseDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TicketPurchaseDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> purchaseTicket(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateTicketPurchaseDto.class)
                .flatMap(createTicketPurchaseDto -> ticketPurchaseService.purchaseTicket(serverRequest.principal(), createTicketPurchaseDto))
                .flatMap(ticketPurchase -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(ticketPurchase))
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases by username",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesForLoggedUser(ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMapMany(principal -> ticketPurchaseService.getAllTicketPurchasesByUser(principal.getName()))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases by city for logged user",
            parameters = {@Parameter(in = ParameterIn.PATH, name = "city", description = "city name")},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesForLoggedUserByCityName(ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMapMany(principal -> ticketPurchaseService.getAllTicketPurchasesByUserAndCity(principal.getName(), serverRequest.pathVariable("city")))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases by cinema",
            parameters = {@Parameter(in = ParameterIn.PATH, name = "cinemaId", description = "cinema id")},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesByCinemaId(ServerRequest serverRequest) {
        return ticketPurchaseService
                .getAllTicketPurchaseByCinema(serverRequest.pathVariable("cinemaId"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases by city",
            parameters = {@Parameter(in = ParameterIn.PATH, name = "city", description = "city name")},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesByCity(ServerRequest serverRequest) {
        return ticketPurchaseService.getAllTicketPurchasesByCity(serverRequest.pathVariable("city"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases by cinema for logged user",
            parameters = {@Parameter(in = ParameterIn.PATH, name = "cinemaId", description = "cinema id")},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesForUserByCinemaId(ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMapMany(principal -> ticketPurchaseService.getAllTicketPurchasesByCinemaAndUsername(serverRequest.pathVariable("cinemaId"), principal.getName()))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchases(ServerRequest serverRequest) {
        return ticketPurchaseService.getAllTicketPurchases()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases by date",
            parameters = {@Parameter(name = "from", description = "date from", in = ParameterIn.QUERY, required = false), @Parameter(name = "to", description = "date to", in = ParameterIn.QUERY, required = false)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesByDate(ServerRequest serverRequest) {
        return ticketPurchaseService.getAllTicketPurchasesByDate(serverRequest.queryParam("from"), serverRequest.queryParam("to"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases with movie",
            parameters = {@Parameter(name = "movieId", description = "movie id", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesWithMovieId(ServerRequest serverRequest) {
        return ticketPurchaseService.getAllTicketPurchasesWithMovieId(serverRequest.pathVariable("movieId"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases with movie for logged user",
            parameters = {@Parameter(name = "movieId", description = "movie id", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesWithMovieIdForLoggedUser(ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMapMany(principal -> ticketPurchaseService.getAllTicketPurchasesForUsernameAndMovieId(principal.getName(), serverRequest.pathVariable("movieId")))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET all ticket purchases with cinemaHall id",
            parameters = {@Parameter(name = "cinemaHallId", description = "cinemaHall id", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketPurchaseDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllTicketPurchasesByCinemaHallId(ServerRequest serverRequest) {
        return ticketPurchaseService.getAllTicketPurchasesByCinemaHallId(serverRequest.pathVariable("cinemaHallId"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, TicketPurchaseDto.class)
                );
    }
}
