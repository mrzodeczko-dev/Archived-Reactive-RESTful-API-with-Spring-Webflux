package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.TicketOrderDto;
import com.rzodeczko.domain.ticket_order.TicketOrder;

import java.util.stream.Collectors;

public final class TicketOrderMapper {

    private TicketOrderMapper() {
    }

    public static TicketOrderDto toDto(TicketOrder o) {
        if (o == null) return null;
        return TicketOrderDto.builder()
                .id(o.getId())
                .username(o.getUser() == null ? null : o.getUser().getUsername())
                .movieEmissionDto(MovieEmissionMapper.toDto(o.getMovieEmission()))
                .orderDate(o.getOrderDate())
                .ticketGroupType(o.getTicketGroupType())
                .ticketOrderStatus(o.getTicketOrderStatus())
                .tickets(o.getTickets() == null ? null :
                        o.getTickets().stream().map(TicketMapper::toDto).collect(Collectors.toList()))
                .build();
    }
}
