package com.rzodeczko.domain.city;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.generic.GenericEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

public class City implements GenericEntity {

    private String id;
    private String name;
    private List<Cinema> cinemas;

    public City() {
    }

    public City(String id, String name, List<Cinema> cinemas) {
        this.id = id;
        this.name = name;
        this.cinemas = cinemas;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Cinema> getCinemas() { return cinemas; }
    public void setCinemas(List<Cinema> cinemas) { this.cinemas = cinemas; }

    public City addCinema(Cinema cinema) {
        if (isNull(cinemas)) {
            cinemas = new ArrayList<>(Collections.singletonList(cinema));
        } else {
            cinemas.add(cinema);
        }
        return this;
    }

    public static class Builder {
        private String id;
        private String name;
        private List<Cinema> cinemas;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder cinemas(List<Cinema> cinemas) { this.cinemas = cinemas; return this; }
        public City build() { return new City(id, name, cinemas); }
    }
}
