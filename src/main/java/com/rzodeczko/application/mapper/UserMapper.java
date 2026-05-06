package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.UserDto;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.domain.security.User;

import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .birthDate(nonNull(user.getBirthDate()) ? user.getBirthDate().toString() : null)
                .favoriteMovies(isNull(user.getFavoriteMovies()) ? Collections.emptyList() :
                        user.getFavoriteMovies()
                                .stream()
                                .map(MovieMapper::toDto)
                                .collect(Collectors.toList()))
                .email(user.getEmail())
                .build();
    }

    public static UserDto toDto(Admin admin) {
        if (admin == null) {
            return null;
        }
        return UserDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .favoriteMovies(Collections.emptyList())
                .role(admin.getRole().name())
                .build();
    }
}
