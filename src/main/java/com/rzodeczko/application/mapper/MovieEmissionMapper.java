package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.domain.movie_emission.MovieEmission;

public final class MovieEmissionMapper {

    private MovieEmissionMapper() {
    }

    public static MovieEmissionDto toDto(MovieEmission me) {
        if (me == null) return null;
        return MovieEmissionDto.builder()
                .id(me.getId())
                .movieId(me.getMovie() == null ? null : me.getMovie().getId())
                .startTime(me.getStartDateTime())
                .cinemaHallId(me.getCinemaHallId())
                .isPositionFree(me.getIsPositionFree())
                .baseTicketPrice(me.getBaseTicketPrice() == null ? null : me.getBaseTicketPrice().getValue().toString())
                .build();
    }
}
