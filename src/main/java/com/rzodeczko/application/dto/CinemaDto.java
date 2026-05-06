package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CinemaDto {

    private String id;
    private String city;
    private String street;
    private Map<String, Integer> hallsCapacity;
}
