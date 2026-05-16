package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateTicketOrderDto;
import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTicketsOrderDtoValidatorTest {

    private final CreateTicketsOrderDtoValidator validator = new CreateTicketsOrderDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid order: no errors")
        void shouldReturnNoErrorsForValidOrder() {
            assertThat(validator.validate(validDto())).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Missing ticket group type: ticketOrderType error")
        void shouldReturnErrorWhenTicketGroupTypeIsNull() {
            CreateTicketOrderDto dto = CreateTicketOrderDto.builder()
                    .movieEmissionId("emission-1")
                    .ticketsDetails(List.of(ticketDetails(1, 1)))
                    .ticketGroupType(null)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("ticketOrderType", "is null");
        }

        @Test
        @DisplayName("Invalid base fields and missing group type: all errors returned")
        void shouldReturnBaseAndOrderErrors() {
            CreateTicketOrderDto dto = CreateTicketOrderDto.builder()
                    .movieEmissionId(null)
                    .ticketsDetails(List.of())
                    .ticketGroupType(null)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("movieEmissionId {null}", "is not valid")
                    .containsEntry("ticketDetails {[]}", "are not valid")
                    .containsEntry("ticketOrderType", "is null");
        }
    }

    private CreateTicketOrderDto validDto() {
        return CreateTicketOrderDto.builder()
                .movieEmissionId("emission-1")
                .ticketsDetails(List.of(ticketDetails(1, 1), ticketDetails(1, 2)))
                .ticketGroupType(TicketGroupType.NORMAL)
                .build();
    }

    private TicketDetailsDto ticketDetails(Integer row, Integer column) {
        return TicketDetailsDto.builder()
                .individualTicketType(IndividualTicketType.REGULAR)
                .position(new Position(row, column))
                .build();
    }
}
