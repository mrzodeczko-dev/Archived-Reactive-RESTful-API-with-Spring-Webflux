package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.CinemaHallsHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CinemaHallsRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/cinemaHalls", method = RequestMethod.GET, beanClass = CinemaHallsHandler.class, beanMethod = "getAll"),
            @RouterOperation(path = "/cinemaHalls/cinemaId/{cinemaId}", method = RequestMethod.GET, beanClass = CinemaHallsHandler.class, beanMethod = "getAllForCinema"),
            @RouterOperation(path = "/cinemaHalls/addToCinema/cinemaId/{cinemaId}", method = RequestMethod.POST, beanClass = CinemaHallsHandler.class, beanMethod = "addCinemaHallToCinema")
    })
    public RouterFunction<ServerResponse> cinemaHallsRouterFunction(CinemaHallsHandler cinemaHallsHandler) {
        return route()
                .path("/cinemaHalls", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .GET("", _ -> cinemaHallsHandler.getAll())
                                .GET("/cinemaId/{cinemaId}", cinemaHallsHandler::getAllForCinema)
                                .POST("/addToCinema/cinemaId/{cinemaId}", cinemaHallsHandler::addCinemaHallToCinema)
                        )
                )
                .build();
    }
}