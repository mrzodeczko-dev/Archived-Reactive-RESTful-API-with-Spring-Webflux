package com.app.infrastructure.routing.handlers;

import com.app.application.dto.*;
import com.app.application.service.StatisticsService;
import com.app.infrastructure.aspect.annotations.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StatisticsHandler {

    private final StatisticsService statisticsService;

    @Loggable
    @Operation(summary = "GET cinema frequency by city for all cities")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CityFrequencyDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getCinemaFrequencyByCityForAllCities(final ServerRequest serverRequest) {
        return statisticsService.findCitiesFrequency()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, CityFrequencyDto.class)
                );
    }

    @Loggable
    @Operation(summary = "GET city with max cinema frequency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CityFrequencyDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getCityWithMaxFrequency(final ServerRequest serverRequest) {
        return statisticsService.findCitiesWithMostFrequency()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, CityFrequencyDto.class)
                );
    }

    @Loggable
    @Operation(summary = "GET most popular movie grouped by city")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MostPopularMovieGroupedByCityDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> findMostPopularMovieGroupedByCity(final ServerRequest serverRequest) {
        return statisticsService.findMostPopularMovieGroupedByCity()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MostPopularMovieGroupedByCityDto.class)
                );
    }

    @Loggable
    @Operation(summary = "GET all movie frequency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieFrequencyDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> findAllMoviesFrequency(final ServerRequest serverRequest) {
        return statisticsService.findAllMoviesFrequency()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieFrequencyDto.class)
                );
    }

    @Loggable
    @Operation(summary = "GET most popular movies gruped by genre in city", parameters = @Parameter(name = "city", in = ParameterIn.PATH, description = "city name"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MovieFrequencyDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> findMostPopularMoviesGroupedByGenreInCity(final ServerRequest serverRequest) {
        return statisticsService.findMostPopularMoviesGroupedByGenreInCity(serverRequest.pathVariable("city"))
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, MovieFrequencyDto.class)
                );
    }

    @Loggable
    @Operation(summary = "GET average ticket price grouped by city")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AverageTicketPriceByCityDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })
    })
    public Mono<ServerResponse> getAverageTicketPriceGroupedByCity(final ServerRequest serverRequest) {
        return statisticsService.getAverageTicketPriceGroupedByCity()
                .as(flux -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux, AverageTicketPriceByCityDto.class)
                );
    }
}
