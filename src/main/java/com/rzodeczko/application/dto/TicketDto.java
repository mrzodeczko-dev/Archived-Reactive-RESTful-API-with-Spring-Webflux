package com.rzodeczko.application.dto;

import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;

public record TicketDto(
        String id,
        TicketStatus ticketStatus,
        IndividualTicketType type,
        Position position,
        Discount discount,
        Money price
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private TicketStatus ticketStatus;
        private IndividualTicketType type;
        private Position position;
        private Discount discount;
        private Money price;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder ticketStatus(TicketStatus ticketStatus) {
            this.ticketStatus = ticketStatus;
            return this;
        }

        public Builder type(IndividualTicketType type) {
            this.type = type;
            return this;
        }

        public Builder position(Position position) {
            this.position = position;
            return this;
        }

        public Builder discount(Discount discount) {
            this.discount = discount;
            return this;
        }

        public Builder price(Money price) {
            this.price = price;
            return this;
        }

        public TicketDto build() {
            return new TicketDto(id, ticketStatus, type, position, discount, price);
        }
    }
}