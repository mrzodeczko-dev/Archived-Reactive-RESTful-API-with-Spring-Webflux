package com.rzodeczko.presentation.routing.handlers;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.application.dto.ResponseErrorDto;
import com.rzodeczko.application.exception.MovieEmissionServiceException;
import com.rzodeczko.application.service.MovieEmissionService;
import com.rzodeczko.infrastructure.aspect.annotations.Loggable;
import com.rzodeczko.presentation.csv.CsvMultipartFileReader;
import com.rzodeczko.presentation.dto.CsvFileUploadRequest;
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

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MovieEmissionsHandler {


    private final MovieEmissionService movieEmissionService;
    private final CsvMultipartFileReader csvMultipartFileReader;

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
            summary = "POST add movie emissions with csv",
            requestBody = @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = CsvFileUploadRequest.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieEmissionDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })})
    public Mono<ServerResponse> addMovieEmissionsWithCsvFile(ServerRequest serverRequest) {
        return csvMultipartFileReader.readCsvFile(serverRequest, movieEmissionService::uploadCSVFile, MovieEmissionServiceException::new)
                .collectList()
                .flatMap(movieEmissions -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movieEmissions)));
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
    public Mono<ServerResponse> getAllMovieEmissions() {

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
            summary = "GET movie emissions by cinema and movie with optional date filtering",
            parameters = {
                    @Parameter(name = "cinemaId", in = ParameterIn.PATH, description = "Cinema ID"),
                    @Parameter(name = "movieId", in = ParameterIn.PATH, description = "Movie ID"),
                    @Parameter(name = "fromDate", in = ParameterIn.QUERY, description = "Optional: from date (ISO 8601)", required = false),
                    @Parameter(name = "toDate", in = ParameterIn.QUERY, description = "Optional: to date (ISO 8601)", required = false)
            },
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = MovieEmissionDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getMovieEmissionsByCinemaAndMovie(ServerRequest serverRequest) {
        String cinemaId = serverRequest.pathVariable("cinemaId");
        String movieId = serverRequest.pathVariable("movieId");
        LocalDateTime fromDate = serverRequest.queryParam("fromDate")
                .map(LocalDateTime::parse)
                .orElse(null);
        LocalDateTime toDate = serverRequest.queryParam("toDate")
                .map(LocalDateTime::parse)
                .orElse(null);

        return movieEmissionService.getMovieEmissionsByCinemaAndMovie(cinemaId, movieId, fromDate, toDate)
                .collectList()
                .flatMap(emissions -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(emissions))
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