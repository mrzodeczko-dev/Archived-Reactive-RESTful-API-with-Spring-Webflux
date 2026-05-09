package com.rzodeczko.presentation.routing;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

public abstract class BaseJsonRouter {

    protected RequestPredicate jsonAccept() {
        return accept(MediaType.APPLICATION_JSON);
    }

    protected RequestPredicate jsonContent() {
        return contentType(MediaType.APPLICATION_JSON);
    }

    protected RequestPredicate jsonAcceptAndContent() {
        return jsonAccept().and(jsonContent());
    }
}