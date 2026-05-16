package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.LoginHandler;
import com.rzodeczko.presentation.routing.handlers.RefreshHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class LoginRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/login", method = RequestMethod.POST, beanClass = LoginHandler.class, beanMethod = "login"),
            @RouterOperation(path = "/refresh", method = RequestMethod.POST, beanClass = RefreshHandler.class, beanMethod = "refresh")
    })
    public RouterFunction<ServerResponse> loginRouterFunction(LoginHandler loginHandler, RefreshHandler refreshHandler) {
        return route()
                .POST("/login", jsonAccept(), loginHandler::login)
                .POST("/refresh", jsonAccept(), refreshHandler::refresh)
                .build();
    }
}