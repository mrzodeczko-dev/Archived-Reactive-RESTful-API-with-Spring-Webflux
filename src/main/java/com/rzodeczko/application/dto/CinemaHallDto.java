package com.rzodeczko.application.dto;

import java.util.List;

public record CinemaHallDto(
        String id,
        String cinemaId,
        Integer rowNo,
        Integer colNo,
        List<MovieEmissionDto> movieEmissions
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String cinemaId;
        private Integer rowNo;
        private Integer colNo;
        private List<MovieEmissionDto> movieEmissions;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder cinemaId(String cinemaId) {
            this.cinemaId = cinemaId;
            return this;
        }

        public Builder rowNo(Integer rowNo) {
            this.rowNo = rowNo;
            return this;
        }

        public Builder colNo(Integer colNo) {
            this.colNo = colNo;
            return this;
        }

        public Builder movieEmissions(List<MovieEmissionDto> movieEmissions) {
            this.movieEmissions = movieEmissions;
            return this;
        }

        public CinemaHallDto build() {
            return new CinemaHallDto(id, cinemaId, rowNo, colNo, movieEmissions);
        }
    }
}