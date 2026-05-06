package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CinemaHallDto {

    private String id;
    private String cinemaId;

    private Integer rowNo;
    private Integer colNo;

    private List<MovieEmissionDto> movieEmissions;

}
