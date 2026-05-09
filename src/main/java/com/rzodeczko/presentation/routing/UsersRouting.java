package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.UsersHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UsersRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/register", method = RequestMethod.POST, beanClass = UsersHandler.class, beanMethod = "register"),
            @RouterOperation(path = "/users", method = RequestMethod.GET, beanClass = UsersHandler.class, beanMethod = "getAllUsers"),
            @RouterOperation(path = "/users/username/{username}", method = RequestMethod.GET, beanClass = UsersHandler.class, beanMethod = "getByUsername"),
            @RouterOperation(path = "/users/promoteToAdmin/username/{username}", method = RequestMethod.POST, beanClass = UsersHandler.class, beanMethod = "promoteUserToAdminRole")
    })
    public RouterFunction<ServerResponse> usersRoute(UsersHandler usersHandler) {
        return route()
                .POST("/register", jsonAccept(), usersHandler::register)
                .path("/users", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .GET("", usersHandler::getAllUsers)
                                .GET("/username/{username}", usersHandler::getByUsername)
                                .POST("/promoteToAdmin/username/{username}", usersHandler::promoteUserToAdminRole)
                        )
                )
                .build();
    }
}