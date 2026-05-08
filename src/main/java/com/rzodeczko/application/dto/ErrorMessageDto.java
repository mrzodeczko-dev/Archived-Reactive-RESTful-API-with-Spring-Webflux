package com.rzodeczko.application.dto;

public record ErrorMessageDto(
        String message
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorMessageDto build() {
            return new ErrorMessageDto(message);
        }
    }
}