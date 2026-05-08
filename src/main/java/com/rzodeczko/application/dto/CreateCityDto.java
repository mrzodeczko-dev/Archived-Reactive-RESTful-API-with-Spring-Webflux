package com.rzodeczko.application.dto;

import com.rzodeczko.domain.city.City;

import java.util.ArrayList;

public record CreateCityDto(
        String name
) {
    public City toEntity() {
        return City.builder()
                .name(name)
                .cinemas(new ArrayList<>())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public CreateCityDto build() {
            return new CreateCityDto(name);
        }
    }
}