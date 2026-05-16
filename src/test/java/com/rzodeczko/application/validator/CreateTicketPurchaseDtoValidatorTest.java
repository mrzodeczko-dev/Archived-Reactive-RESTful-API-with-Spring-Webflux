package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
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

class CreateTicketPurchaseDtoValidatorTest {

    private final CreateTicketPurchaseDtoValidator validator = new CreateTicketPurchaseDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid purchase: no errors")
        void shouldReturnNoErrorsForValidPurchase() {
            assertThat(validator.validate(validDto())).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Blank movie emission id: movieEmissionId error")
        void shouldReturnErrorWhenMovieEmissionIdIsBlank() {
            CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                    .movieEmissionId("")
                    .ticketsDetails(List.of(ticketDetails(1, 1)))
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("movieEmissionId {}", "is not valid");
        }

        @Test
        @DisplayName("Empty ticket details: ticketDetails error")
        void shouldReturnErrorWhenTicketDetailsAreEmpty() {
            CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                    .movieEmissionId("emission-1")
                    .ticketsDetails(List.of())
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("ticketDetails {[]}", "are not valid");
        }

        @Test
        @DisplayName("Duplicate ticket positions: ticketDetails error")
        void shouldReturnErrorWhenTicketPositionsAreDuplicated() {
            CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                    .movieEmissionId("emission-1")
                    .ticketsDetails(List.of(ticketDetails(2, 3), ticketDetails(2, 3)))
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .build();

            assertThat(validator.validate(dto))
                    .anySatisfy((key, value) -> {
                        assertThat(key).startsWith("ticketDetails {[TicketDetailsDto");
                        assertThat(value).isEqualTo("are not valid");
                    });
        }
    }

    private CreateTicketPurchaseDto validDto() {
        return CreateTicketPurchaseDto.builder()
                .movieEmissionId("emission-1")
                .ticketsDetails(List.of(ticketDetails(1, 1), ticketDetails(1, 2)))
                .ticketGroupType(TicketGroupType.FAMILY)
                .build();
    }

    private TicketDetailsDto ticketDetails(Integer row, Integer column) {
        return TicketDetailsDto.builder()
                .individualTicketType(IndividualTicketType.REGULAR)
                .position(new Position(row, column))
                .build();
    }
}
