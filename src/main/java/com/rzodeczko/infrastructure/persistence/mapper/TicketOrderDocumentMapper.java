package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.infrastructure.persistence.document.TicketOrderDocument;

public final class TicketOrderDocumentMapper {

    private TicketOrderDocumentMapper() {
    }

    public static TicketOrderDocument toDocument(TicketOrder o) {
        if (o == null) return null;
        return new TicketOrderDocument(
                o.getId(),
                UserDocumentMapper.toDocument(o.getUser()),
                o.getOrderDate(),
                o.getTicketOrderStatus(),
                MovieEmissionDocumentMapper.toDocument(o.getMovieEmission()),
                TicketDocumentMapper.toDocuments(o.getTickets()),
                o.getTicketGroupType());
    }

    public static TicketOrder toDomain(TicketOrderDocument doc) {
        if (doc == null) return null;
        return TicketOrder.builder()
                .id(doc.getId())
                .user(UserDocumentMapper.toUserDomain(doc.getUser()))
                .orderDate(doc.getOrderDate())
                .ticketOrderStatus(doc.getTicketOrderStatus())
                .movieEmission(MovieEmissionDocumentMapper.toDomain(doc.getMovieEmission()))
                .tickets(TicketDocumentMapper.toDomains(doc.getTickets()))
                .ticketGroupType(doc.getTicketGroupType())
                .build();
    }
}
