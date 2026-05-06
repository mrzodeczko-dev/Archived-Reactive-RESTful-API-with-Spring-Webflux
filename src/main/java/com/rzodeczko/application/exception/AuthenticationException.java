package com.rzodeczko.application.exception;

public class AuthenticationException extends HandledException{

    public AuthenticationException(String message) {
        super(message);
    }
}
