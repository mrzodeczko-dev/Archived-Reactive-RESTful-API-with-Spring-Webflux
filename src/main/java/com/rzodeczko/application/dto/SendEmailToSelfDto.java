package com.rzodeczko.application.dto;

/**
 * Request body for the "send email to logged user" endpoint.
 *
 * <p>Intentionally does <b>not</b> contain a {@code to} field — the recipient is always the
 * authenticated user, and the address is resolved server-side from the JWT principal. Exposing
 * a {@code to} input would only invite confusion (it was previously ignored anyway).
 */
public record SendEmailToSelfDto(
        String title,
        String htmlContent
) {
    public CreateMailDto toCreateMailDto(String resolvedTo) {
        return new CreateMailDto(resolvedTo, htmlContent, title);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String htmlContent;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder htmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public SendEmailToSelfDto build() {
            return new SendEmailToSelfDto(title, htmlContent);
        }
    }
}