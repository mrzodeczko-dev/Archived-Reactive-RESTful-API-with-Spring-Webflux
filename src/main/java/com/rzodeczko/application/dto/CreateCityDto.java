package com.rzodeczko.application.dto;

import com.rzodeczko.domain.city.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateCityDto {

    private String name;

    public City toEntity(){
        return City.builder()
                .name(name)
                .cinemas(new ArrayList<>())
                .build();
    }
}
