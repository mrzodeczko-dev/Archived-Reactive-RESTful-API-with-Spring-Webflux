package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.EmailHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class EmailRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/emails/send/single", beanClass = EmailHandler.class, beanMethod = "sendSingleEmail"),
            @RouterOperation(method = RequestMethod.POST, path = "/emails/send/multiple", beanClass = EmailHandler.class, beanMethod = "sendMultipleEmails")
    })
    public RouterFunction<ServerResponse> emailsRouterFunction(EmailHandler emailHandler) {
        return route()
                .path("/emails", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .POST("/send/single", emailHandler::sendSingleEmail)
                                .POST("/send/multiple", emailHandler::sendMultipleEmails)
                        )
                )
                .build();
    }
}