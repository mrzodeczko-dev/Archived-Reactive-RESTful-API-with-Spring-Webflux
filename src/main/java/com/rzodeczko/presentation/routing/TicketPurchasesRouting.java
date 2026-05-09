package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.TicketPurchaseHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TicketPurchasesRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/ticketPurchases/ticketOrderId/{ticketOrderId}", beanClass = TicketPurchaseHandler.class, beanMethod = "purchaseTicketFromOrder"),
            @RouterOperation(method = RequestMethod.POST, path = "/ticketPurchases", beanClass = TicketPurchaseHandler.class, beanMethod = "purchaseTicket"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesForLoggedUser"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases/city/{city}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesForLoggedUserByCityName"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases/cinemaId/{cinemaId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesForUserByCinemaId"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/cinemaId/{cinemaId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByCinemaId"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/city/{city}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchases"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/dates", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByDate"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/movieId/{movieId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesWithMovieId"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases/movieId/{movieId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesWithMovieIdForLoggedUser"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/cinemaHallId/{cinemaHallId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByCinemaHallId")
    })
    public RouterFunction<ServerResponse> ticketPurchasesRouting(TicketPurchaseHandler ticketPurchaseHandler) {
        return route()
                .path("/ticketPurchases", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .POST("/ticketOrderId/{ticketOrderId}", ticketPurchaseHandler::purchaseTicketFromOrder)
                                .POST("", ticketPurchaseHandler::purchaseTicket)
                                .GET("", ticketPurchaseHandler::getAllTicketPurchasesForLoggedUser)
                                .GET("/city/{city}", ticketPurchaseHandler::getAllTicketPurchasesForLoggedUserByCityName)
                                .GET("/cinemaId/{cinemaId}", ticketPurchaseHandler::getAllTicketPurchasesForUserByCinemaId)
                                .GET("/movieId/{movieId}", ticketPurchaseHandler::getAllTicketPurchasesWithMovieIdForLoggedUser)
                        )
                )
                .path("/admin/ticketPurchases", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .GET("/cinemaId/{cinemaId}", ticketPurchaseHandler::getAllTicketPurchasesByCinemaId)
                                .GET("/city/{city}", ticketPurchaseHandler::getAllTicketPurchasesByCity)
                                .GET("", ticketPurchaseHandler::getAllTicketPurchases)
                                .GET("/dates", ticketPurchaseHandler::getAllTicketPurchasesByDate)
                                .GET("/movieId/{movieId}", ticketPurchaseHandler::getAllTicketPurchasesWithMovieId)
                                .GET("/cinemaHallId/{cinemaHallId}", ticketPurchaseHandler::getAllTicketPurchasesByCinemaHallId)
                        )
                )
                .build();
    }
}