package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.CinemaInCityDto;
import com.rzodeczko.application.dto.CityDto;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;

import java.util.stream.Collectors;

public final class CityMapper {

    private CityMapper() {
    }

    public static CityDto toDto(City city) {
        if (city == null) return null;
        return CityDto.builder()
                .id(city.getId())
                .name(city.getName())
                .cinemas(city.getCinemas() == null ? null :
                        city.getCinemas().stream()
                                .map(cinema -> CinemaInCityDto.builder()
                                        .id(cinema.getId())
                                        .cinemaHallsCapacities(cinema.getCinemaHalls() == null ? null :
                                                cinema.getCinemaHalls()
                                                        .stream()
                                                        .collect(Collectors.toMap(
                                                                CinemaHall::getId,
                                                                e -> e.getPositions().size())))
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }
}
