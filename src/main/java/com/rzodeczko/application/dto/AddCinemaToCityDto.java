package com.rzodeczko.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AddCinemaToCityDto {

    private String city;
    private String street;
    private List<CreateCinemaHallDto> cinemaHallsCapacity;
}
