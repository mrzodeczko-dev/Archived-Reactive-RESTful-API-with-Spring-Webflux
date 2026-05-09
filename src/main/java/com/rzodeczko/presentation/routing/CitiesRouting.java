package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.CitiesHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CitiesRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/cities", beanClass = CitiesHandler.class, beanMethod = "addCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/cities/name/{name}", beanClass = CitiesHandler.class, beanMethod = "findByName"),
            @RouterOperation(method = RequestMethod.GET, path = "/cities", beanClass = CitiesHandler.class, beanMethod = "getAll"),
            @RouterOperation(method = RequestMethod.PUT, path = "/cities", beanClass = CitiesHandler.class, beanMethod = "addCinemaToCity")
    })
    public RouterFunction<ServerResponse> citiesRouting(CitiesHandler citiesHandler) {
        return route()
                .path("/cities", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .POST("", citiesHandler::addCity)
                                .GET("/name/{name}", citiesHandler::findByName)
                                .GET("", citiesHandler::getAll)
                                .PUT("", citiesHandler::addCinemaToCity)
                        )
                )
                .build();
    }
}