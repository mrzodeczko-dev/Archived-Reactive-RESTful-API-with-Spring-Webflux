package com.rzodeczko.application.dto;

import java.util.Map;

public record CinemaDto(
        String id,
        String city,
        String street,
        Map<String, Integer> hallsCapacity
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String city;
        private String street;
        private Map<String, Integer> hallsCapacity;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder hallsCapacity(Map<String, Integer> hallsCapacity) {
            this.hallsCapacity = hallsCapacity;
            return this;
        }

        public CinemaDto build() {
            return new CinemaDto(id, city, street, hallsCapacity);
        }
    }
}