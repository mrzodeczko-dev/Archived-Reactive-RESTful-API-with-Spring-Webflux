package com.rzodeczko.application.dto;

public record CreateMovieEmissionDto(
        String movieId,
        String cinemaHallId,
        String startTime,
        String baseTicketPrice
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String movieId;
        private String cinemaHallId;
        private String startTime;
        private String baseTicketPrice;

        public Builder movieId(String movieId) {
            this.movieId = movieId;
            return this;
        }

        public Builder cinemaHallId(String cinemaHallId) {
            this.cinemaHallId = cinemaHallId;
            return this;
        }

        public Builder startTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder baseTicketPrice(String baseTicketPrice) {
            this.baseTicketPrice = baseTicketPrice;
            return this;
        }

        public CreateMovieEmissionDto build() {
            return new CreateMovieEmissionDto(movieId, cinemaHallId, startTime, baseTicketPrice);
        }
    }
}