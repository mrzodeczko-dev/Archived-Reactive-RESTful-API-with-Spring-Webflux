package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCinemaDto {

    private String city;
    private String street;
    private List<CreateCinemaHallDto> cinemaHallsCapacity;
}
