package com.rzodeczko.application.dto;

public record MovieFrequencyDto(
        MovieDto movie,
        Integer frequency
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MovieDto movie;
        private Integer frequency;

        public Builder movie(MovieDto movie) {
            this.movie = movie;
            return this;
        }

        public Builder frequency(Integer frequency) {
            this.frequency = frequency;
            return this;
        }

        public MovieFrequencyDto build() {
            return new MovieFrequencyDto(movie, frequency);
        }
    }
}