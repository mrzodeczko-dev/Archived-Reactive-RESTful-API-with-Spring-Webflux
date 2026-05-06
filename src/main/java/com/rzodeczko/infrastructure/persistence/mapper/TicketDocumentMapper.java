package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.infrastructure.persistence.document.TicketDocument;

import java.util.List;
import java.util.stream.Collectors;

public final class TicketDocumentMapper {

    private TicketDocumentMapper() {
    }

    public static TicketDocument toDocument(Ticket t) {
        if (t == null) return null;
        return new TicketDocument(
                t.getId(),
                t.getTicketStatus(),
                t.getType(),
                t.getPosition(),
                t.getDiscount(),
                t.getPrice());
    }

    public static Ticket toDomain(TicketDocument doc) {
        if (doc == null) return null;
        return Ticket.builder()
                .id(doc.getId())
                .ticketStatus(doc.getTicketStatus())
                .type(doc.getType())
                .position(doc.getPosition())
                .discount(doc.getDiscount())
                .price(doc.getPrice())
                .build();
    }

    public static List<TicketDocument> toDocuments(List<Ticket> list) {
        if (list == null) return null;
        return list.stream().map(TicketDocumentMapper::toDocument).collect(Collectors.toList());
    }

    public static List<Ticket> toDomains(List<TicketDocument> list) {
        if (list == null) return null;
        return list.stream().map(TicketDocumentMapper::toDomain).collect(Collectors.toList());
    }
}
