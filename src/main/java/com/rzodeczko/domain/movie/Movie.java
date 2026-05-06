package com.rzodeczko.domain.movie;

import com.rzodeczko.domain.generic.GenericEntity;

import java.time.LocalDate;
import java.util.Objects;

public class Movie implements GenericEntity {

    private String id;
    private String name;
    private String genre;
    private Integer duration;
    private LocalDate premiereDate;

    public Movie() {
    }

    public Movie(String id, String name, String genre, Integer duration, LocalDate premiereDate) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.duration = duration;
        this.premiereDate = premiereDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public LocalDate getPremiereDate() { return premiereDate; }
    public void setPremiereDate(LocalDate premiereDate) { this.premiereDate = premiereDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        Movie m = (Movie) o;
        return Objects.equals(id, m.id)
                && Objects.equals(name, m.name)
                && Objects.equals(genre, m.genre)
                && Objects.equals(duration, m.duration)
                && Objects.equals(premiereDate, m.premiereDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, genre, duration, premiereDate);
    }

    public static class Builder {
        private String id;
        private String name;
        private String genre;
        private Integer duration;
        private LocalDate premiereDate;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder genre(String genre) { this.genre = genre; return this; }
        public Builder duration(Integer duration) { this.duration = duration; return this; }
        public Builder premiereDate(LocalDate premiereDate) { this.premiereDate = premiereDate; return this; }
        public Movie build() { return new Movie(id, name, genre, duration, premiereDate); }
    }
}
