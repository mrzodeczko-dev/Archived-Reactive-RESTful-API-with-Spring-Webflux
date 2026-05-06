package com.rzodeczko.application.mapper;

import com.rzodeczko.application.dto.TicketPurchaseDto;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;

import java.util.stream.Collectors;

public final class TicketPurchaseMapper {

    private TicketPurchaseMapper() {
    }

    public static TicketPurchaseDto toDto(TicketPurchase ticketPurchase) {
        if (ticketPurchase == null) {
            return null;
        }
        return TicketPurchaseDto.builder()
                .id(ticketPurchase.getId())
                .username(ticketPurchase.getUser() == null ? null : ticketPurchase.getUser().getUsername())
                .movieEmissionDto(MovieEmissionMapper.toDto(ticketPurchase.getMovieEmission()))
                .purchaseDate(ticketPurchase.getPurchaseDate())
                .tickets(ticketPurchase.getTickets() == null ? null :
                        ticketPurchase.getTickets().stream().map(TicketMapper::toDto).collect(Collectors.toList()))
                .ticketGroupType(ticketPurchase.getTicketGroupType())
                .build();
    }
}
