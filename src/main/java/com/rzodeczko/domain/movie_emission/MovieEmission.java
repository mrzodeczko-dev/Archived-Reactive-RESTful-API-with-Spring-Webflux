package com.rzodeczko.domain.movie_emission;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MovieEmission(
        String id,
        Movie movie,
        LocalDateTime startDateTime,
        Money baseTicketPrice,
        String cinemaHallId,
        Map<Position, Boolean> isPositionFree,
        Long version
) implements GenericEntity {

    public MovieEmission {
        isPositionFree = isPositionFree == null ? new LinkedHashMap<>() : new LinkedHashMap<>(isPositionFree);
    }

    public MovieEmission() {
        this(null, null, null, null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public MovieEmission setId(String id) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public Movie getMovie() {
        return movie;
    }

    public MovieEmission setMovie(Movie movie) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public MovieEmission setStartDateTime(LocalDateTime startDateTime) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public Money getBaseTicketPrice() {
        return baseTicketPrice;
    }

    public MovieEmission setBaseTicketPrice(Money baseTicketPrice) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public String getCinemaHallId() {
        return cinemaHallId;
    }

    public MovieEmission setCinemaHallId(String cinemaHallId) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public Map<Position, Boolean> getIsPositionFree() {
        return isPositionFree;
    }

    public MovieEmission setIsPositionFree(Map<Position, Boolean> isPositionFree) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public MovieEmission setVersion(Long version) {
        return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
    }

    public List<Position> getFreePositions() {
        return isPositionFree
                .entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .collect(ArrayList::new, (list, entry) -> list.add(entry.getKey()), ArrayList::addAll);
    }

    public MovieEmission removeFreePositions(List<Position> positions) {
        var updatedPositionFree = new LinkedHashMap<>(isPositionFree);
        Optional
                .ofNullable(positions)
                .map(Collection::stream)
                .ifPresent(stream -> stream
                        .forEach(position -> updatedPositionFree.computeIfPresent(position, (key, value) -> false)));
        return setIsPositionFree(updatedPositionFree);
    }

    public static class Builder {
        private String id;
        private Movie movie;
        private LocalDateTime startDateTime;
        private Money baseTicketPrice;
        private String cinemaHallId;
        private Map<Position, Boolean> isPositionFree;
        private Long version;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder movie(Movie movie) {
            this.movie = movie;
            return this;
        }

        public Builder startDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public Builder baseTicketPrice(Money baseTicketPrice) {
            this.baseTicketPrice = baseTicketPrice;
            return this;
        }

        public Builder cinemaHallId(String cinemaHallId) {
            this.cinemaHallId = cinemaHallId;
            return this;
        }

        public Builder isPositionFree(Map<Position, Boolean> isPositionFree) {
            this.isPositionFree = isPositionFree;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public MovieEmission build() {
            return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree, version);
        }
    }
}
