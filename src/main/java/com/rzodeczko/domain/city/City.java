package com.rzodeczko.domain.city;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.domain.generic.GenericEntity;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public record City(
        String id,
        String name,
        List<Cinema> cinemas
) implements GenericEntity {

    public City {
        cinemas = cinemas == null ? new ArrayList<>() : new ArrayList<>(cinemas);
    }

    public City() {
        this(null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public City setId(String id) { return new City(id, name, cinemas); }
    public String getName() { return name; }
    public City setName(String name) { return new City(id, name, cinemas); }
    public List<Cinema> getCinemas() { return cinemas; }
    public City setCinemas(List<Cinema> cinemas) { return new City(id, name, cinemas); }

    public City addCinema(Cinema cinema) {
        var updatedCinemas = isNull(cinemas) ? new ArrayList<Cinema>() : new ArrayList<>(cinemas);
        updatedCinemas.add(cinema);
        return setCinemas(updatedCinemas);
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
