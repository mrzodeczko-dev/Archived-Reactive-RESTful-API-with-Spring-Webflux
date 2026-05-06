package com.rzodeczko.domain.cinema_hall;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

public class CinemaHall implements GenericEntity {

    private String id;
    private List<Position> positions;
    private String cinemaId;
    private List<MovieEmission> movieEmissions;

    public CinemaHall() {
    }

    public CinemaHall(String id, List<Position> positions, String cinemaId, List<MovieEmission> movieEmissions) {
        this.id = id;
        this.positions = positions;
        this.cinemaId = cinemaId;
        this.movieEmissions = movieEmissions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Position> getPositions() { return positions; }
    public void setPositions(List<Position> positions) { this.positions = positions; }
    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String cinemaId) { this.cinemaId = cinemaId; }
    public List<MovieEmission> getMovieEmissions() { return movieEmissions; }
    public void setMovieEmissions(List<MovieEmission> movieEmissions) { this.movieEmissions = movieEmissions; }

    public CinemaHall addMovieEmission(MovieEmission movieEmission) {
        if (nonNull(movieEmissions)) {
            movieEmissions.add(movieEmission);
        } else {
            movieEmissions = new ArrayList<>(Collections.singletonList(movieEmission));
        }
        return this;
    }

    public CinemaHall removeMovieEmissionById(String movieEmissionId) {
        if (nonNull(movieEmissions)) {
            movieEmissions.removeIf(movieEmission -> movieEmission.getId().equals(movieEmissionId));
        }
        return this;
    }

    public static class Builder {
        private String id;
        private List<Position> positions;
        private String cinemaId;
        private List<MovieEmission> movieEmissions;

        public Builder id(String id) { this.id = id; return this; }
        public Builder positions(List<Position> positions) { this.positions = positions; return this; }
        public Builder cinemaId(String cinemaId) { this.cinemaId = cinemaId; return this; }
        public Builder movieEmissions(List<MovieEmission> movieEmissions) { this.movieEmissions = movieEmissions; return this; }
        public CinemaHall build() { return new CinemaHall(id, positions, cinemaId, movieEmissions); }
    }
}
