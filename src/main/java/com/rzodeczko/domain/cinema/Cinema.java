package com.rzodeczko.domain.cinema;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.generic.GenericEntity;

import java.util.List;

import static java.util.Objects.nonNull;

public class Cinema implements GenericEntity {

    private String id;
    private String city;
    private String street;
    private List<CinemaHall> cinemaHalls;

    public Cinema() {
    }

    public Cinema(String id, String city, String street, List<CinemaHall> cinemaHalls) {
        this.id = id;
        this.city = city;
        this.street = street;
        this.cinemaHalls = cinemaHalls;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCity() { return city; }
    public Cinema setCity(String city) { this.city = city; return this; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public List<CinemaHall> getCinemaHalls() { return cinemaHalls; }
    public void setCinemaHalls(List<CinemaHall> cinemaHalls) { this.cinemaHalls = cinemaHalls; }

    public Cinema setCinemasIdForCinemaHalls(String cinemaId) {
        if (nonNull(cinemaHalls)) {
            cinemaHalls.forEach(cinemaHall -> cinemaHall.setCinemaId(cinemaId));
        }
        return this;
    }

    public static class Builder {
        private String id;
        private String city;
        private String street;
        private List<CinemaHall> cinemaHalls;

        public Builder id(String id) { this.id = id; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder street(String street) { this.street = street; return this; }
        public Builder cinemaHalls(List<CinemaHall> cinemaHalls) { this.cinemaHalls = cinemaHalls; return this; }
        public Cinema build() { return new Cinema(id, city, street, cinemaHalls); }
    }
}
