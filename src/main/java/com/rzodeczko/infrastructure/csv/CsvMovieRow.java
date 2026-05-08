package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvBindByName;
import com.rzodeczko.application.dto.CreateMovieDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CsvMovieRow {

    @CsvBindByName
    private String genre;

    @CsvBindByName
    private String name;

    @CsvBindByName
    private Integer duration;

    @CsvBindByName(format = "yyyy-MM-dd")
    private String premiereDate;

    public CreateMovieDto toApplicationDto() {
        return new CreateMovieDto(genre, name, duration, premiereDate);
    }
}