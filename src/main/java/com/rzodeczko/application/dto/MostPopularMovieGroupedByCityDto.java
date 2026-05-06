package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MostPopularMovieGroupedByCityDto {

    private String city;
    private List<MovieFrequencyDto> movieFrequency;
}
