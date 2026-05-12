package com.rzodeczko.domain.cinema;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.generic.GenericEntity;

import java.util.ArrayList;
import java.util.List;

public record Cinema(
        String id,
        String cityId,
        String street,
        List<CinemaHall> cinemaHalls
) implements GenericEntity {

    public Cinema {
        cinemaHalls = cinemaHalls == null ? new ArrayList<>() : new ArrayList<>(cinemaHalls);
    }

    public Cinema() {
        this(null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public Cinema setId(String id) {
        return new Cinema(id, cityId, street, cinemaHalls);
    }

    public String getCityId() {
        return cityId;
    }

    public Cinema setCityId(String cityId) {
        return new Cinema(id, cityId, street, cinemaHalls);
    }

    public String getStreet() {
        return street;
    }

    public Cinema setStreet(String street) {
        return new Cinema(id, cityId, street, cinemaHalls);
    }

    public List<CinemaHall> getCinemaHalls() {
        return cinemaHalls;
    }

    public Cinema setCinemaHalls(List<CinemaHall> cinemaHalls) {
        return new Cinema(id, cityId, street, cinemaHalls);
    }

    public Cinema setCinemasIdForCinemaHalls(String cinemaId) {
        if (cinemaHalls == null) {
            return this;
        }
        var updatedCinemaHalls = cinemaHalls.stream()
                .map(cinemaHall -> cinemaHall.setCinemaId(cinemaId))
                .toList();
        return setCinemaHalls(updatedCinemaHalls);
    }

    public Cinema addCinemaHall(CinemaHall cinemaHall) {
        var updatedCinemaHalls = new ArrayList<>(cinemaHalls);
        updatedCinemaHalls.add(cinemaHall);
        return setCinemaHalls(updatedCinemaHalls);
    }

    public static class Builder {
        private String id;
        private String cityId;
        private String street;
        private List<CinemaHall> cinemaHalls;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder city(String city) {
            this.cityId = city;
            return this;
        }

        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder cinemaHalls(List<CinemaHall> cinemaHalls) {
            this.cinemaHalls = cinemaHalls;
            return this;
        }

        public Cinema build() {
            return new Cinema(id, cityId, street, cinemaHalls);
        }
    }
}
