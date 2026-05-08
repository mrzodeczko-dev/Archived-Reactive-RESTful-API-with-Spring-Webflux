package com.rzodeczko.application.dto;

public record CreateMailDto(
        String to,
        String htmlContent,
        String title
) {
    public MailDto toMailDto() {
        return MailDto.builder()
                .title(title)
                .to(to)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String htmlContent;
        private String title;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder htmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public CreateMailDto build() {
            return new CreateMailDto(to, htmlContent, title);
        }
    }
}