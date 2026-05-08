package com.rzodeczko.application.dto;

import java.util.Map;

public record CinemaInCityDto(
        String id,
        Map<String, Integer> cinemaHallsCapacities
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Map<String, Integer> cinemaHallsCapacities;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder cinemaHallsCapacities(Map<String, Integer> cinemaHallsCapacities) {
            this.cinemaHallsCapacities = cinemaHallsCapacities;
            return this;
        }

        public CinemaInCityDto build() {
            return new CinemaInCityDto(id, cinemaHallsCapacities);
        }
    }
}
