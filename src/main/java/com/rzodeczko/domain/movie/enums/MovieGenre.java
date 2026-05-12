package com.rzodeczko.domain.movie.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MovieGenre {

    DRAMA("Drama"),
    COMEDY("Comedy"),
    ACTION("Action"),
    THRILLER("Thriller"),
    DARK_COMEDY("Dark comedy");

    private final String desc;

    MovieGenre(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static List<String> getAllMovieGenres() {
        return Arrays.stream(MovieGenre.values())
                .map(MovieGenre::getDesc)
                .collect(Collectors.toList());
    }

    public static MovieGenre fromDesc(String desc) {
        return Arrays.stream(MovieGenre.values())
                .filter(movieGenre -> movieGenre.desc.equals(desc))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown movie genre: " + desc));
    }
}
