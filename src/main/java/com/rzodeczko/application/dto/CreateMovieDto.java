package com.rzodeczko.application.dto;

import com.rzodeczko.domain.movie.Movie;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record CreateMovieDto(
        String genre,
        String name,
        Integer duration,
        String premiereDate
) {
    public Movie toEntity() {
        return Movie.builder()
                .duration(duration)
                .genre(genre)
                .name(name)
                .premiereDate(LocalDate.parse(premiereDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String genre;
        private String name;
        private Integer duration;
        private String premiereDate;

        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder premiereDate(String premiereDate) {
            this.premiereDate = premiereDate;
            return this;
        }

        public CreateMovieDto build() {
            return new CreateMovieDto(genre, name, duration, premiereDate);
        }
    }
}