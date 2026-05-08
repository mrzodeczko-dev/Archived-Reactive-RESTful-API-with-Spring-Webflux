package com.rzodeczko.application.dto;

public record MovieFrequencyByGenreDto(
        String genre,
        Integer frequency
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String genre;
        private Integer frequency;

        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder frequency(Integer frequency) {
            this.frequency = frequency;
            return this;
        }

        public MovieFrequencyByGenreDto build() {
            return new MovieFrequencyByGenreDto(genre, frequency);
        }
    }
}