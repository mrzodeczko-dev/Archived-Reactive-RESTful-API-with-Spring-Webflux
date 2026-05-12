package com.rzodeczko.application.csv;

import java.util.List;


public record ParseResult<T>(List<T> items, List<String> errors) {

    public ParseResult {
        items = List.copyOf(items);
        errors = List.copyOf(errors);
    }

    public static <T> ParseResult<T> of(List<T> items, List<String> errors) {
        return new ParseResult<>(items, errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}