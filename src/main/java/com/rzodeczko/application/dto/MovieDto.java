package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MovieDto {

    private String id;
    private String name;
    private String genre;
    private Integer duration;
    private LocalDate premiereDate;
}
