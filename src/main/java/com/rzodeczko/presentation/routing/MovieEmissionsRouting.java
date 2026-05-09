package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.MovieEmissionsHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class MovieEmissionsRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/movieEmissions", beanClass = MovieEmissionsHandler.class, beanMethod = "addMovieEmission"),
            @RouterOperation(method = RequestMethod.GET, path = "/movieEmissions", beanClass = MovieEmissionsHandler.class, beanMethod = "getAllMovieEmissions"),
            @RouterOperation(method = RequestMethod.GET, path = "/movieEmissions/movieId/{movieId}", beanClass = MovieEmissionsHandler.class, beanMethod = "getAllMovieEmissionsByMovieId"),
            @RouterOperation(method = RequestMethod.GET, path = "/movieEmissions/cinemaHallId/{cinemaHallId}", beanClass = MovieEmissionsHandler.class, beanMethod = "getAllMovieEmissionsByCinemaHallId"),
            @RouterOperation(method = RequestMethod.DELETE, path = "/movieEmissions/{id}", beanClass = MovieEmissionsHandler.class, beanMethod = "deleteMovieEmissionById")
    })
    public RouterFunction<ServerResponse> movieEmissionsRouterFunction(MovieEmissionsHandler movieEmissionsHandler) {
        return route()
                .path("/movieEmissions", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .POST("", movieEmissionsHandler::addMovieEmission)
                                .GET("", movieEmissionsHandler::getAllMovieEmissions)
                                .GET("/movieId/{movieId}", movieEmissionsHandler::getAllMovieEmissionsByMovieId)
                                .GET("/cinemaHallId/{cinemaHallId}", movieEmissionsHandler::getAllMovieEmissionsByCinemaHallId)
                                .DELETE("/{id}", movieEmissionsHandler::deleteMovieEmissionById)
                        )
                )
                .build();
    }
}