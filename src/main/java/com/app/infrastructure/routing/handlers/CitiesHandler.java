package com.app.infrastructure.routing.handlers;

import com.app.application.dto.*;
import com.app.application.service.CityService;
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

import java.util.List;

@Component
@RequiredArgsConstructor
public class CitiesHandler {

    private final CityService cityService;

    @Loggable
    @Operation(
            summary = "POST add city",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateCityDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CityDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> addCity(ServerRequest request) {

        return cityService.addCity(request.bodyToMono(CreateCityDto.class))
                .flatMap(savedCity -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(savedCity))
                );
    }

    @Loggable
    @Operation(
            summary = "GET city by name",
            parameters = {@Parameter(in = ParameterIn.PATH, name = "name", description = "City name")},
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CityDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> findByName(ServerRequest request) {

        return cityService.findByName(request.pathVariable("name"))
                .flatMap(city -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(city)));

    }

    @Loggable
    @Operation(
            summary = "GET all cities",
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CityDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {

        return cityService.getAll()
                .collectList()
                .flatMap(cities -> ServerResponse
                        .status(HttpStatus.OK)
                        .header("Access-Control-Allow-Origin", "*")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(cities)));
    }

    @Loggable
    @Operation(
            summary = "POST add city",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateCityDto.class))),
            security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AddCinemaToCityDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> addCinemaToCity(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(AddCinemaToCityDto.class)
                .flatMap(dto -> cityService.addCinemaToCity(dto)
                        .flatMap(city -> ServerResponse.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue((city))))

                );
    }
}
