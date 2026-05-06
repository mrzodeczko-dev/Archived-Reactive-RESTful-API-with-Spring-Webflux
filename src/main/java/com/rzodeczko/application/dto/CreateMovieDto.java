package com.rzodeczko.application.dto;

import com.rzodeczko.domain.movie.Movie;
import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateMovieDto {

    @CsvBindByName
    private String genre;

    @CsvBindByName
    private String name;

    @CsvBindByName
    private Integer duration;

    @CsvBindByName(format = "yyyy-MM-dd")
    private String premiereDate;

    public Movie toEntity() {
        return Movie.builder()
                .duration(duration)
                .genre(genre)
                .name(name)
                .premiereDate(LocalDate.parse(premiereDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .build();

    }

}
