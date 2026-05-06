package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class AddCinemaHallToCinemaDto {

    private Integer rowNo;
    private Integer colNo;
    private String cinemaId;
}
