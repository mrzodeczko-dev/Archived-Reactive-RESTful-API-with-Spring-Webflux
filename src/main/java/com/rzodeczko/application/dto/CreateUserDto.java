package com.rzodeczko.application.dto;

import com.rzodeczko.domain.security.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record CreateUserDto(
        String username,
        String password,
        String passwordConfirmation,
        String birthDate,
        String email
) {
    public User toEntity() {
        return User.builder()
                .password(password)
                .birthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .username(username)
                .email(email)
                .build();
    }

    public CreateUserDto withPassword(String newPassword) {
        return new CreateUserDto(username, newPassword, passwordConfirmation, birthDate, email);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private String passwordConfirmation;
        private String birthDate;
        private String email;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder passwordConfirmation(String passwordConfirmation) {
            this.passwordConfirmation = passwordConfirmation;
            return this;
        }

        public Builder birthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public CreateUserDto build() {
            return new CreateUserDto(username, password, passwordConfirmation, birthDate, email);
        }
    }
}