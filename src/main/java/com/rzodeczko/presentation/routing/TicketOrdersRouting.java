package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.TicketOrderHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TicketOrdersRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/ticketOrders", method = RequestMethod.POST, beanClass = TicketOrderHandler.class, beanMethod = "orderTickets"),
            @RouterOperation(path = "/ticketsOrders/cancel/orderId/{orderId}", method = RequestMethod.PUT, beanClass = TicketOrderHandler.class, beanMethod = "cancelOrder"),
            @RouterOperation(path = "/ticketsOrders/username", method = RequestMethod.GET, beanClass = TicketOrderHandler.class, beanMethod = "getAllTicketOrdersByUsername")
    })
    public RouterFunction<ServerResponse> ticketOrdersRouting(TicketOrderHandler ticketOrderHandler) {
        return route()
                .POST("/ticketOrders", jsonAccept(), ticketOrderHandler::orderTickets)
                .path("/ticketsOrders", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .PUT("/cancel/orderId/{orderId}", ticketOrderHandler::cancelOrder)
                                .GET("/username", ticketOrderHandler::getAllTicketOrdersByUsername)
                        )
                )
                .build();
    }
}