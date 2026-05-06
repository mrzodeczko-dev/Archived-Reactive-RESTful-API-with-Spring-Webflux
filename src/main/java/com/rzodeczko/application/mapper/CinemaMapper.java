package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.CinemaDto;
import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.cinema_hall.CinemaHall;

import java.util.stream.Collectors;

public final class CinemaMapper {

    private CinemaMapper() {
    }

    public static CinemaDto toDto(Cinema cinema) {
        if (cinema == null) return null;
        return CinemaDto.builder()
                .id(cinema.getId())
                .city(cinema.getCity())
                .street(cinema.getStreet())
                .hallsCapacity(cinema.getCinemaHalls() == null ? null :
                        cinema.getCinemaHalls()
                                .stream()
                                .collect(Collectors.toMap(
                                        CinemaHall::getId,
                                        e -> e.getPositions().size())))
                .build();
    }
}
