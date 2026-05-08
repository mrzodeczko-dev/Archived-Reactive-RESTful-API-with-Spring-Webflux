package com.rzodeczko.application.dto;

public record MailDto(
        String to,
        String title
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String title;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public MailDto build() {
            return new MailDto(to, title);
        }
    }
}