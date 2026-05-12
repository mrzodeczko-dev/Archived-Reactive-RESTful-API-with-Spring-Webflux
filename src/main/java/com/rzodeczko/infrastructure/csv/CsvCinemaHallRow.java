package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvBindByName;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CsvCinemaHallRow {

    @CsvBindByName
    private Integer rowNo;

    @CsvBindByName
    private Integer colNo;

    public CreateCinemaHallDto toApplicationDto() {
        return new CreateCinemaHallDto(rowNo, colNo);
    }
}
