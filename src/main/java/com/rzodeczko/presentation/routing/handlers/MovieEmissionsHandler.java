package com.rzodeczko.presentation.routing.handlers;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.application.service.MovieEmissionService;
import com.rzodeczko.infrastructure.aspect.annotations.Loggable;
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
public class MovieEmissionsHandler {


    private final MovieEmissionService movieEmissionService;

    @Loggable
    @Operation(
            summary = "POST add movie emission",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateMovieEmissionDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(schema = @Schema(implementation = MovieEmissionDto.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> addMovieEmission(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(CreateMovieEmissionDto.class)
                .flatMap(movieEmissionService::createMovieEmission)
                .flatMap(savedVal -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(savedVal))
                );

    }

    @Loggable
    @Operation(
            summary = "GET all movie emissions",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = MovieEmissionDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAllMovieEmissions(ServerRequest serverRequest) {

        return movieEmissionService.getAllMovieEmissions()
                .collectList()
                .flatMap(list -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(list))
                );
    }

    @Loggable
    @Operation(
            summary = "GET all movie emissions by movie id",
            parameters = {@Parameter(name = "movieId", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = MovieEmissionDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAllMovieEmissionsByMovieId(ServerRequest serverRequest) {

        return movieEmissionService.getAllMovieEmissionsByMovieId(serverRequest.pathVariable("movieId"))
                .collectList()
                .flatMap(movieEmissions -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movieEmissions))
                );
    }

    @Loggable
    @Operation(
            summary = "GET all movie emissions by cinemaHall id",
            parameters = {@Parameter(name = "cinemaHallId", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = MovieEmissionDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAllMovieEmissionsByCinemaHallId(ServerRequest serverRequest) {

        return movieEmissionService.getAllMovieEmissionsByCinemaHallId(serverRequest.pathVariable("cinemaHallId"))
                .collectList()
                .flatMap(movieEmissions -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movieEmissions))
                );
    }

    @Loggable
    @Operation(
            summary = "DELETE movie emission by id",
            parameters = {@Parameter(name = "id", in = ParameterIn.PATH)},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(schema = @Schema(implementation = MovieEmissionDto.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> deleteMovieEmissionById(ServerRequest serverRequest) {

        return movieEmissionService.deleteMovieEmission(serverRequest.pathVariable("id"))
                .flatMap(movieEmission -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movieEmission))
                );
    }

}
