package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvBindByName;
import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CsvMovieEmissionRow {

    @CsvBindByName
    private String movieId;

    @CsvBindByName
    private String cinemaHallId;

    @CsvBindByName
    private String startTime;

    @CsvBindByName
    private String baseTicketPrice;

    public CreateMovieEmissionDto toApplicationDto() {
        return new CreateMovieEmissionDto(movieId, cinemaHallId, startTime, baseTicketPrice);
    }
}
