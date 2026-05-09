package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.LoginHandler;
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
            @RouterOperation(path = "/login", method = RequestMethod.POST, beanClass = LoginHandler.class, beanMethod = "login")
    })
    public RouterFunction<ServerResponse> loginRouterFunction(LoginHandler loginHandler) {
        return route()
                .POST("/login", jsonAccept(), loginHandler::login)
                .build();
    }
}