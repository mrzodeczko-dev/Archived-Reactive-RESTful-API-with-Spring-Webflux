package com.rzodeczko.application.dto;

import java.time.LocalDate;

public record MovieDto(
        String id,
        String name,
        String genre,
        Integer duration,
        LocalDate premiereDate
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String genre;
        private Integer duration;
        private LocalDate premiereDate;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder premiereDate(LocalDate premiereDate) {
            this.premiereDate = premiereDate;
            return this;
        }

        public MovieDto build() {
            return new MovieDto(id, name, genre, duration, premiereDate);
        }
    }
}