package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import com.rzodeczko.infrastructure.persistence.document.TicketDocument;
import com.rzodeczko.infrastructure.persistence.document.TicketPurchaseDocument;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketPurchaseDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(TicketPurchaseDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Ticket purchase domain: nested values mapped")
        void shouldMapDomainToDocument() {
            TicketPurchaseDocument document = TicketPurchaseDocumentMapper.toDocument(domainPurchase());

            assertThat(document.getId()).isEqualTo("purchase-1");
            assertThat(document.getUser().getUsername()).isEqualTo("jan@example.com");
            assertThat(document.getPurchaseDate()).isEqualTo(LocalDate.of(2026, 6, 2));
            assertThat(document.getMovieEmission().getId()).isEqualTo("emission-1");
            assertThat(document.getTickets()).hasSize(1);
            assertThat(document.getTicketGroupType()).isEqualTo(TicketGroupType.NORMAL);
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(TicketPurchaseDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Ticket purchase document: nested values mapped")
        void shouldMapDocumentToDomain() {
            TicketPurchase domain = TicketPurchaseDocumentMapper.toDomain(documentPurchase());

            assertThat(domain.getId()).isEqualTo("purchase-1");
            assertThat(domain.getUser().getUsername()).isEqualTo("jan@example.com");
            assertThat(domain.getPurchaseDate()).isEqualTo(LocalDate.of(2026, 6, 2));
            assertThat(domain.getMovieEmission().getId()).isEqualTo("emission-1");
            assertThat(domain.getTickets()).hasSize(1);
            assertThat(domain.getTicketGroupType()).isEqualTo(TicketGroupType.NORMAL);
        }
    }

    private TicketPurchase domainPurchase() {
        return TicketPurchase.builder()
                .id("purchase-1")
                .user(UserDocumentMapper.toDomain(userDocument()))
                .purchaseDate(LocalDate.of(2026, 6, 2))
                .movieEmission(MovieEmissionDocumentMapper.toDomain(movieEmissionDocument()))
                .tickets(TicketDocumentMapper.toDomains(List.of(ticketDocument())))
                .ticketGroupType(TicketGroupType.NORMAL)
                .build();
    }

    private TicketPurchaseDocument documentPurchase() {
        return new TicketPurchaseDocument(
                "purchase-1",
                userDocument(),
                LocalDate.of(2026, 6, 2),
                movieEmissionDocument(),
                List.of(ticketDocument()),
                TicketGroupType.NORMAL
        );
    }

    private UserDocument userDocument() {
        return new UserDocument("user-1", "jan@example.com", "hashed-pass", Role.ROLE_USER, null, null, "jan@example.com");
    }

    private MovieEmissionDocument movieEmissionDocument() {
        return new MovieEmissionDocument("emission-1", null, LocalDateTime.of(2026, 6, 1, 20, 0), null, "hall-1", null);
    }

    private TicketDocument ticketDocument() {
        return new TicketDocument("ticket-1", null, null, null, null, null);
    }
}
