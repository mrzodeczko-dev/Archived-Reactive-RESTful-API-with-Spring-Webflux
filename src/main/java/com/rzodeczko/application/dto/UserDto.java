package com.rzodeczko.application.dto;

import java.util.List;

public record UserDto(
        String id,
        String username,
        String birthDate,
        String role,
        String email,
        List<MovieDto> favoriteMovies
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String birthDate;
        private String role;
        private String email;
        private List<MovieDto> favoriteMovies;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder birthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder favoriteMovies(List<MovieDto> favoriteMovies) {
            this.favoriteMovies = favoriteMovies;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, username, birthDate, role, email, favoriteMovies);
        }
    }
}