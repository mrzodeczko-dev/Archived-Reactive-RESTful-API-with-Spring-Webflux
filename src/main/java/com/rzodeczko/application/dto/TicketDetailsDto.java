package com.rzodeczko.application.dto;

import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.vo.Position;

public record TicketDetailsDto(
        IndividualTicketType individualTicketType,
        Position position
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IndividualTicketType individualTicketType;
        private Position position;

        public Builder individualTicketType(IndividualTicketType individualTicketType) {
            this.individualTicketType = individualTicketType;
            return this;
        }

        public Builder position(Position position) {
            this.position = position;
            return this;
        }

        public TicketDetailsDto build() {
            return new TicketDetailsDto(individualTicketType, position);
        }
    }
}