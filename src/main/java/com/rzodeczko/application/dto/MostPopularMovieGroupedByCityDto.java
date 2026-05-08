package com.rzodeczko.application.dto;

import java.util.List;

public record MostPopularMovieGroupedByCityDto(
        String city,
        List<MovieFrequencyDto> movieFrequency
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String city;
        private List<MovieFrequencyDto> movieFrequency;

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder movieFrequency(List<MovieFrequencyDto> movieFrequency) {
            this.movieFrequency = movieFrequency;
            return this;
        }

        public MostPopularMovieGroupedByCityDto build() {
            return new MostPopularMovieGroupedByCityDto(city, movieFrequency);
        }
    }
}