package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.MoviesHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class MoviesRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.GET, path = "/movies/id/{id}", beanClass = MoviesHandler.class, beanMethod = "getById"),
            @RouterOperation(method = RequestMethod.PATCH, path = "/movies/addToFavorites/{id}", beanClass = MoviesHandler.class, beanMethod = "addMovieToFavorites"),
            @RouterOperation(method = RequestMethod.POST, path = "/movies", beanClass = MoviesHandler.class, beanMethod = "addMovieToDatabase"),
            @RouterOperation(method = RequestMethod.DELETE, path = "/movies/id/{id}", beanClass = MoviesHandler.class, beanMethod = "deleteMovieById"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies", beanClass = MoviesHandler.class, beanMethod = "getAllMovies"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies/filter/premiereDate", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByPremiereDate"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies/filter/duration", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByDuration"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies/filter/name/{name}", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByName"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies/filter/genre/{genre}", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByGenre"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies/filter/keyword/{keyword}", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByKeyword"),
            @RouterOperation(method = RequestMethod.POST, path = "/movies/csv", beanClass = MoviesHandler.class, beanMethod = "addMovieToDatabaseWithCsvFile"),
            @RouterOperation(method = RequestMethod.GET, path = "/movies/favorites", beanClass = MoviesHandler.class, beanMethod = "getFavoriteMovies")
    })
    public RouterFunction<ServerResponse> moviesRouterFunction(MoviesHandler moviesHandler) {
        return route()
                .path("/movies", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .GET("/id/{id}", moviesHandler::getById)
                                .PATCH("/addToFavorites/{id}", moviesHandler::addMovieToFavorites)
                                .POST("", moviesHandler::addMovieToDatabase)
                                .DELETE("/id/{id}", moviesHandler::deleteMovieById)
                                .GET("", moviesHandler::getAllMovies)
                                .GET("/filter/premiereDate", moviesHandler::getMoviesFilteredByPremiereDate)
                                .GET("/filter/duration", moviesHandler::getMoviesFilteredByDuration)
                                .GET("/filter/name/{name}", moviesHandler::getMoviesFilteredByName)
                                .GET("/filter/genre/{genre}", moviesHandler::getMoviesFilteredByGenre)
                                .GET("/filter/keyword/{keyword}", moviesHandler::getMoviesFilteredByKeyword)
                                .GET("/favorites", moviesHandler::getFavoriteMovies)
                        )
                        .POST("/csv", jsonAccept(), moviesHandler::addMovieToDatabaseWithCsvFile)
                )
                .build();
    }
}