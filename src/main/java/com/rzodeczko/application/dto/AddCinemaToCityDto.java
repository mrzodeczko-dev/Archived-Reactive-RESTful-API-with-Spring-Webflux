package com.rzodeczko.application.dto;

import java.util.List;

public record AddCinemaToCityDto(
        String city,
        String street,
        List<CreateCinemaHallDto> cinemaHallsCapacity
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String city;
        private String street;
        private List<CreateCinemaHallDto> cinemaHallsCapacity;

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder cinemaHallsCapacity(List<CreateCinemaHallDto> cinemaHallsCapacity) {
            this.cinemaHallsCapacity = cinemaHallsCapacity;
            return this;
        }

        public AddCinemaToCityDto build() {
            return new AddCinemaToCityDto(city, street, cinemaHallsCapacity);
        }
    }
}