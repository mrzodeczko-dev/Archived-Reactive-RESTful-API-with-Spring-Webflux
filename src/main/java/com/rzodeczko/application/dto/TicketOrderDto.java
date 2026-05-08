package com.rzodeczko.application.dto;

import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;

import java.time.LocalDate;
import java.util.List;

public record TicketOrderDto(
        String id,
        String username,
        LocalDate orderDate,
        TicketOrderStatus ticketOrderStatus,
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
        private LocalDate orderDate;
        private TicketOrderStatus ticketOrderStatus;
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

        public Builder orderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder ticketOrderStatus(TicketOrderStatus ticketOrderStatus) {
            this.ticketOrderStatus = ticketOrderStatus;
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

        public TicketOrderDto build() {
            return new TicketOrderDto(id, username, orderDate, ticketOrderStatus, movieEmissionDto, tickets, ticketGroupType);
        }
    }
}