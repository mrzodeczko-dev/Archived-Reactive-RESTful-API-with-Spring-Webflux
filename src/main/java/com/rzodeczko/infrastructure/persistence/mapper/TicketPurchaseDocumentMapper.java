package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.infrastructure.persistence.document.TicketPurchaseDocument;

public final class TicketPurchaseDocumentMapper {

    private TicketPurchaseDocumentMapper() {
    }

    public static TicketPurchaseDocument toDocument(TicketPurchase p) {
        if (p == null) return null;
        return new TicketPurchaseDocument(
                p.getId(),
                UserDocumentMapper.toDocument(p.getUser()),
                p.getPurchaseDate(),
                MovieEmissionDocumentMapper.toDocument(p.getMovieEmission()),
                TicketDocumentMapper.toDocuments(p.getTickets()),
                p.getTicketGroupType());
    }

    public static TicketPurchase toDomain(TicketPurchaseDocument doc) {
        if (doc == null) return null;
        return TicketPurchase.builder()
                .id(doc.getId())
                .user(UserDocumentMapper.toUserDomain(doc.getUser()))
                .purchaseDate(doc.getPurchaseDate())
                .movieEmission(MovieEmissionDocumentMapper.toDomain(doc.getMovieEmission()))
                .tickets(TicketDocumentMapper.toDomains(doc.getTickets()))
                .ticketGroupType(doc.getTicketGroupType())
                .build();
    }
}
