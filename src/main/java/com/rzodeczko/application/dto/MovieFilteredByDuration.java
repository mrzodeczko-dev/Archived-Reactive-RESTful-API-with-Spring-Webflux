package com.rzodeczko.application.dto;

public record MovieFilteredByDuration(
        Integer minDuration,
        Integer maxDuration
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer minDuration;
        private Integer maxDuration;

        public Builder minDuration(Integer minDuration) {
            this.minDuration = minDuration;
            return this;
        }

        public Builder maxDuration(Integer maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }

        public MovieFilteredByDuration build() {
            return new MovieFilteredByDuration(minDuration, maxDuration);
        }
    }
}