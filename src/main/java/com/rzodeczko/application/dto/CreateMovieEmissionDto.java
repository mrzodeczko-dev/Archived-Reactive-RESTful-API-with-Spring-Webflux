package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateMovieEmissionDto {

    private String movieId;
    private String cinemaHallId;
    private String startTime;
    private String baseTicketPrice;
}
