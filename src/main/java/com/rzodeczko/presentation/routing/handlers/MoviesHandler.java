package com.rzodeczko.presentation.routing.handlers;

import com.rzodeczko.application.dto.*;
import com.rzodeczko.application.exception.MovieServiceException;
import com.rzodeczko.application.service.MovieService;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MoviesHandler {

    private final MovieService movieService;

    @Loggable
    @Operation(
            summary = "PATCH add movie to favorites",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", description = "movie id"),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MovieDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> addMovieToFavorites(final ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMap(principal -> movieService.addMovieToFavorites(serverRequest.pathVariable("id"), principal.getName()))
                .flatMap(movie -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movie)));
    }

    @Loggable
    @Operation(
            summary = "GET movie by id",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", description = "movie id"),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MovieDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getById(final ServerRequest serverRequest) {
        return movieService.getById(serverRequest.pathVariable("id"))
                .switchIfEmpty(Mono.error(() -> new MovieServiceException("No movie with id : %s".formatted(serverRequest.pathVariable("id")))))
                .flatMap(movie -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movie)));
    }

    @Loggable
    @Operation(
            summary = "POST add movie",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateMovieDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MovieDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> addMovieToDatabase(final ServerRequest serverRequest) {
        return movieService.addMovie(serverRequest.bodyToMono(CreateMovieDto.class))
                .flatMap(movie -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movie))
                );
    }

    @Loggable
    @Operation(
            summary = "POST add movie with csv",
            requestBody = @RequestBody(content = @Content(mediaType = "application/octet-stream", array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })})
    public Mono<ServerResponse> addMovieToDatabaseWithCsvFile(final ServerRequest serverRequest) {
        return movieService.uploadCSVFile(serverRequest.bodyToMono(Resource.class))
                .collectList()
                .flatMap(addedMovieList -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(addedMovieList))
                );
    }

    @Loggable
    @Operation(
            summary = "DELETE movie by id",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", description = "movie id"),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MovieDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> deleteMovieById(final ServerRequest serverRequest) {
        return movieService.deleteMovieById(serverRequest.pathVariable("id"))
                .flatMap(movie -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(movie))
                );
    }

    @Loggable
    @Operation(
            summary = "GET all movies",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAllMovies(ServerRequest serverRequest) {
        return movieService.getAll()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET movies filtered by premiere date",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieFilteredByPremiereDate.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getMoviesFilteredByPremiereDate(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MovieFilteredByPremiereDate.class)
                .flatMap(dto -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(movieService.getFilteredByPremiereDate(dto.getDateFrom(), dto.getDateTo()), MovieDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET movies filtered by duration",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieFilteredByDuration.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getMoviesFilteredByDuration(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MovieFilteredByDuration.class)
                .flatMap(dto -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(movieService.getFilteredByDuration(dto.getMinDuration(), dto.getMaxDuration()), MovieDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET movies filtered by name",
            parameters = @Parameter(name = "name", in = ParameterIn.PATH),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getMoviesFilteredByName(ServerRequest serverRequest) {
        return movieService.getFilteredByName(serverRequest.pathVariable("name"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET movies filtered by genre",
            parameters = @Parameter(name = "genre", in = ParameterIn.PATH),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getMoviesFilteredByGenre(ServerRequest serverRequest) {
        return movieService.getFilteredByGenre(serverRequest.pathVariable("genre"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET movies filtered by keyword",
            parameters = @Parameter(name = "keyword", in = ParameterIn.PATH),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getMoviesFilteredByKeyword(ServerRequest serverRequest) {
        return movieService.getFilteredByKeyword(serverRequest.pathVariable("keyword"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieDto.class)
                );
    }

    @Loggable
    @Operation(
            summary = "GET favorite movies",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getFavoriteMovies(ServerRequest serverRequest) {
        return serverRequest.principal()
                .flatMapMany(principal -> movieService.getFavoriteMovies(principal.getName()))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieDto.class)
                );
    }
}
