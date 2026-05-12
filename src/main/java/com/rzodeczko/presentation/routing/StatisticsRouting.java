package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.StatisticsHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class StatisticsRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/cities/cinemaFrequency", beanClass = StatisticsHandler.class, beanMethod = "getCinemaFrequencyByCityForAllCities"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/cities/cinemaFrequency/max", beanClass = StatisticsHandler.class, beanMethod = "getCityWithMaxFrequency"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/movies/mostPopular/byCity", beanClass = StatisticsHandler.class, beanMethod = "findMostPopularMovieGroupedByCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/movies/frequency", beanClass = StatisticsHandler.class, beanMethod = "findAllMoviesFrequency"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/movies/mostPopularGroupedByGenre/byCity/{city}", beanClass = StatisticsHandler.class, beanMethod = "findMostPopularMoviesGroupedByGenreInCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/averageTicketPrice", beanClass = StatisticsHandler.class, beanMethod = "getAverageTicketPriceGroupedByCity")
    })
    public RouterFunction<ServerResponse> statisticsRouterFunction(StatisticsHandler statisticsHandler) {
        return route()
                .path("/statistics", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .GET("/cities/cinemaFrequency", _ -> statisticsHandler.getCinemaFrequencyByCityForAllCities())
                                .GET("/cities/cinemaFrequency/max", _ -> statisticsHandler.getCityWithMaxFrequency())
                                .GET("/movies/mostPopular/byCity", _ -> statisticsHandler.findMostPopularMovieGroupedByCity())
                                .GET("/movies/frequency", _ -> statisticsHandler.findAllMoviesFrequency())
                                .GET("/movies/mostPopularGroupedByGenre/byCity/{city}", statisticsHandler::findMostPopularMoviesGroupedByGenreInCity)
                                .GET("/averageTicketPrice", _ -> statisticsHandler.getAverageTicketPriceGroupedByCity())
                        )
                )
                .build();
    }
}