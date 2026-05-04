package com.app.infrastructure.routing.handlers;

import com.app.application.dto.AddCinemaHallToCinemaDto;
import com.app.application.dto.CinemaHallDto;
import com.app.application.dto.ResponseErrorDto;
import com.app.application.service.CinemaHallService;
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
public class CinemaHallsHandler {

    private final CinemaHallService cinemaHallService;

    @Loggable
    @Operation(
            summary = "POST add cinemaHall to cinema",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = AddCinemaHallToCinemaDto.class))),
            parameters = @Parameter(in = ParameterIn.PATH, name = "cinemaId"),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CinemaHallDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> addCinemaHallToCinema(ServerRequest serverRequest) {

        return cinemaHallService
                .addCinemaHallToCinema(serverRequest.bodyToMono(AddCinemaHallToCinemaDto.class))
                .flatMap(cinemaHallDto -> ServerResponse.status(HttpStatus.CREATED)
                        .body(BodyInserters.fromValue(cinemaHallDto))
                );

    }

    @Loggable
    @Operation(
            summary = "GET all cinemaHalls that belongs to given cinema",
            parameters = @Parameter(in = ParameterIn.PATH, name = "cinemaId"),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CinemaHallDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAllForCinema(ServerRequest serverRequest) {

        return cinemaHallService
                .getAllForCinema(serverRequest.pathVariable("cinemaId"))
                .collectList()
                .flatMap(list -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(list))
                );

    }

    @Loggable
    @Operation(
            summary = "GET all cinemaHalls",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CinemaHallDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {

        return cinemaHallService
                .getAll()
                .collectList()
                .flatMap(list -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(list))
                );

    }
}
