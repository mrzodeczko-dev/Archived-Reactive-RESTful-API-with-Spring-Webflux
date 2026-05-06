package com.rzodeczko.application.dto;

import com.rzodeczko.application.service.util.ServiceUtils;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateCinemaHallDto {

    private Integer rowNo;
    private Integer colNo;

    public CinemaHall toEntity() {
        return CinemaHall.builder()
                .movieEmissions(new ArrayList<>())
                .positions(ServiceUtils.buildPositions(rowNo, colNo))
                .build();
    }

    public CinemaHall toEntity(String cinemaId) {
        return CinemaHall.builder()
                .cinemaId(cinemaId)
                .movieEmissions(new ArrayList<>())
                .positions(ServiceUtils.buildPositions(rowNo, colNo))
                .build();
    }
}