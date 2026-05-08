package com.rzodeczko.application.dto;

public record CityFrequencyDto(
        Integer frequency,
        String city
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer frequency;
        private String city;

        public Builder frequency(Integer frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public CityFrequencyDto build() {
            return new CityFrequencyDto(frequency, city);
        }
    }
}