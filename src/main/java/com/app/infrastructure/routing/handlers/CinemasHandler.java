package com.app.infrastructure.routing.handlers;

import com.app.application.dto.*;
import com.app.application.service.CinemaService;
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
public class CinemasHandler {

    private final CinemaService cinemaService;

    @Loggable
    @Operation(
            summary = "POST add cinema",
            security = @SecurityRequirement(name = "JwtAuthToken"),
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateCinemaDto.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(schema = @Schema(implementation = CinemaDto.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> addCinema(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateCinemaDto.class)
                .flatMap(cinemaService::addCinema)
                .flatMap(savedCinema -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(savedCinema))
                );
    }

    @Loggable
    @Operation(
            summary = "GET all cinemas",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = CinemaDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        return cinemaService.getAll()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, CinemaDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET cinemas by city",
            parameters = {@Parameter(name = "city", in = ParameterIn.PATH, description = "City name")},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = CinemaDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllCinemasByCity(ServerRequest serverRequest) {
        return cinemaService.getAllByCity(serverRequest.pathVariable("city"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, CinemaDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "PUT add cinemaHall to existing cinema",
            parameters = {@Parameter(name = "id", in = ParameterIn.PATH, description = "cinema id")},
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateCinemaHallDto.class), mediaType = "application/json")),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(schema = @Schema(implementation = CinemaDto.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> addCinemaHall(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateCinemaHallDto.class)
                .flatMap(createCinemaHallDto -> cinemaService.addCinemaHallToCinema(serverRequest.pathVariable("id"), createCinemaHallDto))
                .flatMap(cinemaDto -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(cinemaDto))
                );
    }
}
