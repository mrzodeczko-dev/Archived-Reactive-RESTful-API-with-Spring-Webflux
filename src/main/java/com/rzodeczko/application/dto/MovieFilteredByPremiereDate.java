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
public class MovieFilteredByPremiereDate {

    private LocalDate dateFrom;
    private LocalDate dateTo;
}
