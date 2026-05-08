package com.rzodeczko.application.dto;

import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;

import java.time.LocalDate;
import java.util.List;

public record TicketPurchaseDto(
        String id,
        String username,
        LocalDate purchaseDate,
        MovieEmissionDto movieEmissionDto,
        List<TicketDto> tickets,
        TicketGroupType ticketGroupType
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private LocalDate purchaseDate;
        private MovieEmissionDto movieEmissionDto;
        private List<TicketDto> tickets;
        private TicketGroupType ticketGroupType;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder purchaseDate(LocalDate purchaseDate) {
            this.purchaseDate = purchaseDate;
            return this;
        }

        public Builder movieEmissionDto(MovieEmissionDto movieEmissionDto) {
            this.movieEmissionDto = movieEmissionDto;
            return this;
        }

        public Builder tickets(List<TicketDto> tickets) {
            this.tickets = tickets;
            return this;
        }

        public Builder ticketGroupType(TicketGroupType ticketGroupType) {
            this.ticketGroupType = ticketGroupType;
            return this;
        }

        public TicketPurchaseDto build() {
            return new TicketPurchaseDto(id, username, purchaseDate, movieEmissionDto, tickets, ticketGroupType);
        }
    }
}