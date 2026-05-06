package com.rzodeczko.application.exception;

public class EmailServiceException extends HandledException{
    public EmailServiceException(String message) {
        super(message);
    }
}
