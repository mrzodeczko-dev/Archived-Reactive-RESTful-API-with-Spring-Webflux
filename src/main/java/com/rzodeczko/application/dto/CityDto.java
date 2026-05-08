package com.rzodeczko.application.dto;

import java.util.List;

public record CityDto(
        String id,
        String name,
        List<CinemaInCityDto> cinemas
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private List<CinemaInCityDto> cinemas;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cinemas(List<CinemaInCityDto> cinemas) {
            this.cinemas = cinemas;
            return this;
        }

        public CityDto build() {
            return new CityDto(id, name, cinemas);
        }
    }
}