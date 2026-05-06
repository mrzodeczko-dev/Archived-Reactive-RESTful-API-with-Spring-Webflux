package com.rzodeczko.application.exception;

public class MovieServiceException extends HandledException{
    public MovieServiceException(String message) {
        super(message);
    }
}
