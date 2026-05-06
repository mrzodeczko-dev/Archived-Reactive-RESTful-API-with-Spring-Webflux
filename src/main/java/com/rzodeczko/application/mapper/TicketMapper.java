package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.TicketDto;
import com.rzodeczko.domain.ticket.Ticket;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketDto toDto(Ticket t) {
        if (t == null) return null;
        return TicketDto.builder()
                .id(t.getId())
                .position(t.getPosition())
                .discount(t.getDiscount())
                .type(t.getType())
                .ticketStatus(t.getTicketStatus())
                .price(t.getPrice())
                .build();
    }
}
