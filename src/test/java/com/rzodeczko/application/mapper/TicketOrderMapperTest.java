package com.rzodeczko.application.mapper;

import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
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

class TicketOrderMapperTest {

    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("Null order: null DTO")
        void shouldReturnNullWhenOrderIsNull() {
            assertThat(TicketOrderMapper.toDto(null)).isNull();
        }

        @Test
        @DisplayName("Ticket order: all fields and nested values mapped")
        void shouldMapTicketOrderToDto() {
            TicketOrder order = ticketOrder();

            var dto = TicketOrderMapper.toDto(order);

            assertThat(dto.id()).isEqualTo("order-1");
            assertThat(dto.username()).isEqualTo("jan@example.com");
            assertThat(dto.orderDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(dto.ticketOrderStatus()).isEqualTo(TicketOrderStatus.ORDERED);
            assertThat(dto.ticketGroupType()).isEqualTo(TicketGroupType.FAMILY);
            assertThat(dto.movieEmissionDto().id()).isEqualTo("emission-1");
            assertThat(dto.movieEmissionDto().startTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 20, 0));
            assertThat(dto.tickets()).hasSize(1);
            assertThat(dto.tickets().getFirst().id()).isEqualTo("ticket-1");
            assertThat(dto.tickets().getFirst().ticketStatus()).isEqualTo(TicketStatus.ORDERED);
        }

        @Test
        @DisplayName("Order without user: username mapped as null")
        void shouldMapNullUserAsNullUsername() {
            TicketOrder order = ticketOrder().setUser(null);

            assertThat(TicketOrderMapper.toDto(order).username()).isNull();
        }
    }

    private TicketOrder ticketOrder() {
        return TicketOrder.builder()
                .id("order-1")
                .user(user())
                .orderDate(LocalDate.of(2026, 6, 1))
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .movieEmission(movieEmission())
                .tickets(List.of(ticket(TicketStatus.ORDERED)))
                .ticketGroupType(TicketGroupType.FAMILY)
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

    private Ticket ticket(TicketStatus status) {
        return Ticket.builder()
                .id("ticket-1")
                .ticketStatus(status)
                .type(IndividualTicketType.REGULAR)
                .position(new Position(1, 1))
                .discount(Discount.of("0.0"))
                .price(Money.of("35.00"))
                .build();
    }
}
