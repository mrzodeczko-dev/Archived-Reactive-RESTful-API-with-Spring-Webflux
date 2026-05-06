package com.rzodeczko.domain.security;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.security.enums.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public final class User extends BaseUser {

    private LocalDate birthDate;
    private List<Movie> favoriteMovies;
    private String email;

    public User() {
    }

    public User(String username, String password, LocalDate birthDate, List<Movie> favoriteMovies, String email) {
        super(username, password, Role.ROLE_USER);
        this.birthDate = birthDate;
        this.favoriteMovies = favoriteMovies;
        this.email = email;
    }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public List<Movie> getFavoriteMovies() { return favoriteMovies; }
    public void setFavoriteMovies(List<Movie> favoriteMovies) { this.favoriteMovies = favoriteMovies; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public static RegularUserBuilder builder() {
        return new RegularUserBuilder();
    }

    public User addMovieToFavorites(Movie movie) {
        if (isNull(favoriteMovies)) {
            favoriteMovies = new ArrayList<>();
        }
        favoriteMovies.add(movie);
        return this;
    }

    public static class RegularUserBuilder {
        private String username;
        private String password;
        private String email;
        private LocalDate birthDate;
        private List<Movie> favoriteMovies;

        public RegularUserBuilder username(String username) { this.username = username; return this; }
        public RegularUserBuilder password(String password) { this.password = password; return this; }
        public RegularUserBuilder email(String email) { this.email = email; return this; }
        public RegularUserBuilder birthDate(LocalDate birthDate) { this.birthDate = birthDate; return this; }
        public RegularUserBuilder favoriteMovies(List<Movie> favoriteMovies) { this.favoriteMovies = favoriteMovies; return this; }
        public User build() { return new User(username, password, birthDate, favoriteMovies, email); }
    }
}
