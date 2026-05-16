package com.rzodeczko.application.mapper;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketPurchaseMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null purchase: null DTO")
        void shouldReturnNullWhenPurchaseIsNull() {
            assertThat(TicketPurchaseMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Ticket purchase: all fields and nested values mapped")
        void shouldMapTicketPurchaseToDto() {
            TicketPurchase purchase = ticketPurchase();

            var dto = TicketPurchaseMapper.toDto(purchase);

            assertThat(dto.id()).isEqualTo("purchase-1");
            assertThat(dto.username()).isEqualTo("jan@example.com");
            assertThat(dto.purchaseDate()).isEqualTo(LocalDate.of(2026, 6, 2));
            assertThat(dto.ticketGroupType()).isEqualTo(TicketGroupType.NORMAL);
            assertThat(dto.movieEmissionDto().id()).isEqualTo("emission-1");
            assertThat(dto.movieEmissionDto().startTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 20, 0));
            assertThat(dto.tickets()).hasSize(1);
            assertThat(dto.tickets().getFirst().id()).isEqualTo("ticket-1");
            assertThat(dto.tickets().getFirst().ticketStatus()).isEqualTo(TicketStatus.PURCHASED);
        }

        @Test
        @DisplayName("Purchase without tickets: empty tickets list")
        void shouldMapMissingTicketsToEmptyList() {
            TicketPurchase purchase = ticketPurchase().setTickets(null);

            assertThat(TicketPurchaseMapper.toDto(purchase).tickets()).isEmpty();
        }
    }

    private TicketPurchase ticketPurchase() {
        return TicketPurchase.builder()
                .id("purchase-1")
                .user(user())
                .purchaseDate(LocalDate.of(2026, 6, 2))
                .movieEmission(movieEmission())
                .tickets(List.of(ticket()))
                .ticketGroupType(TicketGroupType.NORMAL)
                .build();
    }

    private User user() {
        return User.builder()
                .id("user-1")
                .username("jan@example.com")
                .password("hashed-pass")
                .role(Role.ROLE_USER)
                .email("jan@example.com")
                .build();
    }

    private MovieEmission movieEmission() {
        return MovieEmission.builder()
                .id("emission-1")
                .startDateTime(LocalDateTime.of(2026, 6, 1, 20, 0))
                .baseTicketPrice(Money.of("35.00"))
                .cinemaHallId("hall-1")
                .build();
    }

    private Ticket ticket() {
        return Ticket.builder()
                .id("ticket-1")
                .ticketStatus(TicketStatus.PURCHASED)
                .type(IndividualTicketType.REGULAR)
                .position(new Position(1, 1))
                .discount(Discount.of("0.0"))
                .price(Money.of("35.00"))
                .build();
    }
}
