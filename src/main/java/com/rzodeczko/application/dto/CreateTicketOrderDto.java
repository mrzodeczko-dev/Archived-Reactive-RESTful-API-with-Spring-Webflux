package com.rzodeczko.application.dto;

import com.rzodeczko.application.dto.contract.TicketDtoMarker;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;

import java.util.List;

public record CreateTicketOrderDto(
        String movieEmissionId,
        List<TicketDetailsDto> ticketsDetails,
        TicketGroupType ticketGroupType
) implements TicketDtoMarker {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String movieEmissionId;
        private List<TicketDetailsDto> ticketsDetails;
        private TicketGroupType ticketGroupType;

        public Builder movieEmissionId(String movieEmissionId) {
            this.movieEmissionId = movieEmissionId;
            return this;
        }

        public Builder ticketsDetails(List<TicketDetailsDto> ticketsDetails) {
            this.ticketsDetails = ticketsDetails;
            return this;
        }

        public Builder ticketGroupType(TicketGroupType ticketGroupType) {
            this.ticketGroupType = ticketGroupType;
            return this;
        }

        public CreateTicketOrderDto build() {
            return new CreateTicketOrderDto(movieEmissionId, ticketsDetails, ticketGroupType);
        }
    }
}