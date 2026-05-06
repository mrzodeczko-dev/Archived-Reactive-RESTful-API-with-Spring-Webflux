package com.rzodeczko.domain.movie_emission;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MovieEmission implements GenericEntity {

    private String id;
    private Movie movie;
    private LocalDateTime startDateTime;
    private Money baseTicketPrice;
    private String cinemaHallId;
    private Map<Position, Boolean> isPositionFree;

    public MovieEmission() {
    }

    public MovieEmission(String id, Movie movie, LocalDateTime startDateTime, Money baseTicketPrice,
                         String cinemaHallId, Map<Position, Boolean> isPositionFree) {
        this.id = id;
        this.movie = movie;
        this.startDateTime = startDateTime;
        this.baseTicketPrice = baseTicketPrice;
        this.cinemaHallId = cinemaHallId;
        this.isPositionFree = isPositionFree;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public Money getBaseTicketPrice() { return baseTicketPrice; }
    public void setBaseTicketPrice(Money baseTicketPrice) { this.baseTicketPrice = baseTicketPrice; }
    public String getCinemaHallId() { return cinemaHallId; }
    public void setCinemaHallId(String cinemaHallId) { this.cinemaHallId = cinemaHallId; }
    public Map<Position, Boolean> getIsPositionFree() { return isPositionFree; }
    public void setIsPositionFree(Map<Position, Boolean> isPositionFree) { this.isPositionFree = isPositionFree; }

    public List<Position> getFreePositions() {
        return isPositionFree
                .entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .collect(ArrayList::new, (list, entry) -> list.add(entry.getKey()), ArrayList::addAll);
    }

    public MovieEmission removeFreePositions(List<Position> positions) {
        Optional
                .ofNullable(positions)
                .map(Collection::stream)
                .ifPresent(stream -> stream
                        .forEach(position -> isPositionFree.computeIfPresent(position, (key, value) -> false)));
        return this;
    }

    public static class Builder {
        private String id;
        private Movie movie;
        private LocalDateTime startDateTime;
        private Money baseTicketPrice;
        private String cinemaHallId;
        private Map<Position, Boolean> isPositionFree;

        public Builder id(String id) { this.id = id; return this; }
        public Builder movie(Movie movie) { this.movie = movie; return this; }
        public Builder startDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; return this; }
        public Builder baseTicketPrice(Money baseTicketPrice) { this.baseTicketPrice = baseTicketPrice; return this; }
        public Builder cinemaHallId(String cinemaHallId) { this.cinemaHallId = cinemaHallId; return this; }
        public Builder isPositionFree(Map<Position, Boolean> isPositionFree) { this.isPositionFree = isPositionFree; return this; }
        public MovieEmission build() { return new MovieEmission(id, movie, startDateTime, baseTicketPrice, cinemaHallId, isPositionFree); }
    }
}
