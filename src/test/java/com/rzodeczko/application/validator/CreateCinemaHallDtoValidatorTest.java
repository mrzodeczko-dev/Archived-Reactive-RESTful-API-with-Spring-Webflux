package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateCinemaHallDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCinemaHallDtoValidatorTest {

    private final CreateCinemaHallDtoValidator validator = new CreateCinemaHallDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid hall capacity: no errors")
        void shouldReturnNoErrorsForValidHallCapacity() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder()
                    .rowNo(5)
                    .colNo(6)
                    .build();

            assertThat(validator.validate(dto)).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Too few columns: colNo error")
        void shouldReturnErrorWhenColumnsAreBelowMinimum() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder()
                    .rowNo(5)
                    .colNo(4)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("colNo", "[4] is not valid. Min required is: 5");
        }

        @Test
        @DisplayName("Too few rows: rowNo error")
        void shouldReturnErrorWhenRowsAreBelowMinimum() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder()
                    .rowNo(4)
                    .colNo(5)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("rowNo", "[4] is not valid. Min required is: 5");
        }

        @Test
        @DisplayName("Null row and column: both errors returned")
        void shouldReturnErrorsWhenRowsAndColumnsAreNull() {
            CreateCinemaHallDto dto = CreateCinemaHallDto.builder()
                    .rowNo(null)
                    .colNo(null)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("rowNo", "[null] is not valid. Min required is: 5")
                    .containsEntry("colNo", "[null] is not valid. Min required is: 5");
        }
    }
}
