package com.rzodeczko.application.dto;

import java.time.LocalDate;

public record MovieFilteredByPremiereDate(
        LocalDate dateFrom,
        LocalDate dateTo
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate dateFrom;
        private LocalDate dateTo;

        public Builder dateFrom(LocalDate dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public Builder dateTo(LocalDate dateTo) {
            this.dateTo = dateTo;
            return this;
        }

        public MovieFilteredByPremiereDate build() {
            return new MovieFilteredByPremiereDate(dateFrom, dateTo);
        }
    }
}