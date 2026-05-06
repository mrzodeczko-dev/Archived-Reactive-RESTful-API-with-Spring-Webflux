package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.MovieDto;
import com.rzodeczko.domain.movie.Movie;

public final class MovieMapper {

    private MovieMapper() {
    }

    public static MovieDto toDto(Movie movie) {
        if (movie == null) return null;
        return MovieDto.builder()
                .id(movie.getId())
                .name(movie.getName())
                .genre(movie.getGenre())
                .premiereDate(movie.getPremiereDate())
                .duration(movie.getDuration())
                .build();
    }
}
