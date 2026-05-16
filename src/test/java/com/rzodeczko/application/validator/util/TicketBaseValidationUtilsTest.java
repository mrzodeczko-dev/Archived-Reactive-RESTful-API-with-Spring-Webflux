package com.rzodeczko.application.validator.util;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TicketBaseValidationUtilsTest {

    @Nested
    @DisplayName("isPositionValid()")
    class IsPositionValidTests {

        @Test
        @DisplayName("Positive row and column: true")
        void shouldReturnTrueForPositiveRowAndColumn() {
            assertThat(TicketBaseValidationUtils.isPositionValid(new Position(1, 2))).isTrue();
        }

        @Test
        @DisplayName("Null position: false")
        void shouldReturnFalseForNullPosition() {
            assertThat(TicketBaseValidationUtils.isPositionValid(null)).isFalse();
        }

        @Test
        @DisplayName("Null coordinate: false")
        void shouldReturnFalseWhenCoordinateIsNull() {
            assertThat(TicketBaseValidationUtils.isPositionValid(new Position(null, 1))).isFalse();
            assertThat(TicketBaseValidationUtils.isPositionValid(new Position(1, null))).isFalse();
        }

        @Test
        @DisplayName("Zero or negative coordinate: false")
        void shouldReturnFalseWhenCoordinateIsNotPositive() {
            assertThat(TicketBaseValidationUtils.isPositionValid(new Position(0, 1))).isFalse();
            assertThat(TicketBaseValidationUtils.isPositionValid(new Position(1, -1))).isFalse();
        }
    }

    @Nested
    @DisplayName("areTicketDetailsValid()")
    class AreTicketDetailsValidTests {

        @Test
        @DisplayName("Valid unique ticket details: true")
        void shouldReturnTrueForValidUniqueTicketDetails() {
            assertThat(TicketBaseValidationUtils.areTicketDetailsValid(List.of(
                    ticketDetails(1, 1),
                    ticketDetails(1, 2)
            ))).isTrue();
        }

        @Test
        @DisplayName("Null or empty list: false")
        void shouldReturnFalseWhenTicketDetailsAreMissing() {
            assertThat(TicketBaseValidationUtils.areTicketDetailsValid(null)).isFalse();
            assertThat(TicketBaseValidationUtils.areTicketDetailsValid(List.of())).isFalse();
        }

        @Test
        @DisplayName("Null detail item: false")
        void shouldReturnFalseWhenTicketDetailItemIsNull() {
            assertThat(TicketBaseValidationUtils.areTicketDetailsValid(Arrays.asList(ticketDetails(1, 1), null))).isFalse();
        }

        @Test
        @DisplayName("Invalid position: false")
        void shouldReturnFalseWhenPositionIsInvalid() {
            assertThat(TicketBaseValidationUtils.areTicketDetailsValid(List.of(
                    TicketDetailsDto.builder()
                            .individualTicketType(IndividualTicketType.REGULAR)
                            .position(new Position(0, 1))
                            .build()
            ))).isFalse();
        }

        @Test
        @DisplayName("Duplicate positions: false")
        void shouldReturnFalseWhenPositionsAreDuplicated() {
            assertThat(TicketBaseValidationUtils.areTicketDetailsValid(List.of(
                    ticketDetails(1, 1),
                    ticketDetails(1, 1)
            ))).isFalse();
        }
    }

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(TicketBaseValidationUtils.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Valid ticket DTO: no errors")
        void shouldReturnNoErrorsForValidTicketDto() {
            assertThat(TicketBaseValidationUtils.validate(validPurchaseDto())).isEmpty();
        }

        @Test
        @DisplayName("Blank movie emission id: movieEmissionId error")
        void shouldReturnErrorWhenMovieEmissionIdIsBlank() {
            CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                    .movieEmissionId(" ")
                    .ticketsDetails(List.of(ticketDetails(1, 1)))
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .build();

            assertThat(TicketBaseValidationUtils.validate(dto))
                    .containsEntry("movieEmissionId { }", "is not valid");
        }

        @Test
        @DisplayName("Invalid ticket details: ticketDetails error")
        void shouldReturnErrorWhenTicketDetailsAreInvalid() {
            CreateTicketPurchaseDto dto = CreateTicketPurchaseDto.builder()
                    .movieEmissionId("emission-1")
                    .ticketsDetails(List.of(ticketDetails(1, 1), ticketDetails(1, 1)))
                    .ticketGroupType(TicketGroupType.NORMAL)
                    .build();

            assertThat(TicketBaseValidationUtils.validate(dto))
                    .anySatisfy((key, value) -> {
                        assertThat(key).startsWith("ticketDetails {[TicketDetailsDto");
                        assertThat(value).isEqualTo("are not valid");
                    });
        }
    }

    private CreateTicketPurchaseDto validPurchaseDto() {
        return CreateTicketPurchaseDto.builder()
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
