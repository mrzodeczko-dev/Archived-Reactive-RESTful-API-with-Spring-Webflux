package com.rzodeczko.domain.cinema_hall;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record CinemaHall(
        String id,
        List<Position> positions,
        String cinemaId,
        List<MovieEmission> movieEmissions
) implements GenericEntity {

    public CinemaHall {
        positions = positions == null ? new ArrayList<>() : new ArrayList<>(positions);
        movieEmissions = movieEmissions == null ? new ArrayList<>() : new ArrayList<>(movieEmissions);
    }

    public CinemaHall() {
        this(null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public CinemaHall setId(String id) {
        return new CinemaHall(id, positions, cinemaId, movieEmissions);
    }

    public List<Position> getPositions() {
        return positions;
    }

    public CinemaHall setPositions(List<Position> positions) {
        return new CinemaHall(id, positions, cinemaId, movieEmissions);
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public CinemaHall setCinemaId(String cinemaId) {
        return new CinemaHall(id, positions, cinemaId, movieEmissions);
    }

    public List<MovieEmission> getMovieEmissions() {
        return movieEmissions;
    }

    public CinemaHall setMovieEmissions(List<MovieEmission> movieEmissions) {
        return new CinemaHall(id, positions, cinemaId, movieEmissions);
    }

    public CinemaHall removeMovieEmissionById(String movieEmissionId) {
        if (movieEmissions == null) {
            return this;
        }
        return setMovieEmissions(movieEmissions.stream()
                .filter(movieEmission -> !Objects.equals(movieEmission.getId(), movieEmissionId))
                .toList());
    }

    public CinemaHall addMovieEmission(MovieEmission movieEmission) {
        var updatedMovieEmissions = new ArrayList<>(movieEmissions);
        updatedMovieEmissions.add(movieEmission);
        return setMovieEmissions(updatedMovieEmissions);
    }

    public static class Builder {
        private String id;
        private List<Position> positions;
        private String cinemaId;
        private List<MovieEmission> movieEmissions;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder positions(List<Position> positions) {
            this.positions = positions;
            return this;
        }

        public Builder cinemaId(String cinemaId) {
            this.cinemaId = cinemaId;
            return this;
        }

        public Builder movieEmissions(List<MovieEmission> movieEmissions) {
            this.movieEmissions = movieEmissions;
            return this;
        }

        public CinemaHall build() {
            return new CinemaHall(id, positions, cinemaId, movieEmissions);
        }
    }
}
