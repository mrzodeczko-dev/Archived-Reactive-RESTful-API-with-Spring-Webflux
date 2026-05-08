package com.rzodeczko.application.dto;

import com.rzodeczko.domain.vo.Position;

import java.time.LocalDateTime;
import java.util.Map;

public record MovieEmissionDto(
        String id,
        String movieId,
        LocalDateTime startTime,
        String cinemaHallId,
        Map<Position, Boolean> isPositionFree,
        String baseTicketPrice
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String movieId;
        private LocalDateTime startTime;
        private String cinemaHallId;
        private Map<Position, Boolean> isPositionFree;
        private String baseTicketPrice;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder movieId(String movieId) {
            this.movieId = movieId;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder cinemaHallId(String cinemaHallId) {
            this.cinemaHallId = cinemaHallId;
            return this;
        }

        public Builder isPositionFree(Map<Position, Boolean> isPositionFree) {
            this.isPositionFree = isPositionFree;
            return this;
        }

        public Builder baseTicketPrice(String baseTicketPrice) {
            this.baseTicketPrice = baseTicketPrice;
            return this;
        }

        public MovieEmissionDto build() {
            return new MovieEmissionDto(id, movieId, startTime, cinemaHallId, isPositionFree, baseTicketPrice);
        }
    }
}