package com.rzodeczko.application.dto;

import java.math.BigDecimal;

public record AverageTicketPriceByCityDto(
        String city,
        BigDecimal averageTicketPrice
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String city;
        private BigDecimal averageTicketPrice;

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder averageTicketPrice(BigDecimal averageTicketPrice) {
            this.averageTicketPrice = averageTicketPrice;
            return this;
        }

        public AverageTicketPriceByCityDto build() {
            return new AverageTicketPriceByCityDto(city, averageTicketPrice);
        }
    }
}