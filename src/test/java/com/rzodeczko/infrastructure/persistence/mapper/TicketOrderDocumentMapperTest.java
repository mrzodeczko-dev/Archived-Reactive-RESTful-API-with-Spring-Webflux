package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import com.rzodeczko.infrastructure.persistence.document.TicketDocument;
import com.rzodeczko.infrastructure.persistence.document.TicketOrderDocument;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketOrderDocumentMapperTest {

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTests {

        @Test
        @DisplayName("Null domain: null document")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(TicketOrderDocumentMapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("Ticket order domain: nested values mapped")
        void shouldMapDomainToDocument() {
            TicketOrderDocument document = TicketOrderDocumentMapper.toDocument(domainOrder());

            assertThat(document.getId()).isEqualTo("order-1");
            assertThat(document.getUser().getUsername()).isEqualTo("jan@example.com");
            assertThat(document.getOrderDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(document.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.ORDERED);
            assertThat(document.getMovieEmission().getId()).isEqualTo("emission-1");
            assertThat(document.getTickets()).hasSize(1);
            assertThat(document.getTicketGroupType()).isEqualTo(TicketGroupType.FAMILY);
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("Null document: null domain")
        void shouldReturnNullWhenDocumentIsNull() {
            assertThat(TicketOrderDocumentMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Ticket order document: nested values mapped")
        void shouldMapDocumentToDomain() {
            TicketOrder domain = TicketOrderDocumentMapper.toDomain(documentOrder());

            assertThat(domain.getId()).isEqualTo("order-1");
            assertThat(domain.getUser().getUsername()).isEqualTo("jan@example.com");
            assertThat(domain.getOrderDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(domain.getTicketOrderStatus()).isEqualTo(TicketOrderStatus.ORDERED);
            assertThat(domain.getMovieEmission().getId()).isEqualTo("emission-1");
            assertThat(domain.getTickets()).hasSize(1);
            assertThat(domain.getTicketGroupType()).isEqualTo(TicketGroupType.FAMILY);
        }
    }

    private TicketOrder domainOrder() {
        return TicketOrder.builder()
                .id("order-1")
                .user(UserDocumentMapper.toDomain(userDocument()))
                .orderDate(LocalDate.of(2026, 6, 1))
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .movieEmission(MovieEmissionDocumentMapper.toDomain(movieEmissionDocument()))
                .tickets(TicketDocumentMapper.toDomains(List.of(ticketDocument())))
                .ticketGroupType(TicketGroupType.FAMILY)
                .build();
    }

    private TicketOrderDocument documentOrder() {
        return new TicketOrderDocument(
                "order-1",
                userDocument(),
                LocalDate.of(2026, 6, 1),
                TicketOrderStatus.ORDERED,
                movieEmissionDocument(),
                List.of(ticketDocument()),
                TicketGroupType.FAMILY
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
