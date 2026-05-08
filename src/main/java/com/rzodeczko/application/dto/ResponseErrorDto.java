package com.rzodeczko.application.dto;

public record ResponseErrorDto(
        ErrorMessageDto error
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ErrorMessageDto error;

        public Builder error(ErrorMessageDto error) {
            this.error = error;
            return this;
        }

        public ResponseErrorDto build() {
            return new ResponseErrorDto(error);
        }
    }
}