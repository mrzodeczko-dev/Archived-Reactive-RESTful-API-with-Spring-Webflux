package com.rzodeczko.application.dto;

import java.util.List;

public record CreateMailsDto(
        List<CreateMailDto> mails
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<CreateMailDto> mails;

        public Builder mails(List<CreateMailDto> mails) {
            this.mails = mails;
            return this;
        }

        public CreateMailsDto build() {
            return new CreateMailsDto(mails);
        }
    }
}