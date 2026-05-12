package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvBindByName;
import com.rzodeczko.application.dto.CreateCityDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CsvCityRow {

    @CsvBindByName
    private String name;

    public CreateCityDto toApplicationDto() {
        return new CreateCityDto(name);
    }
}
