package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCinemaDtoValidatorTest {

    private final CreateCinemaDtoValidator validator = new CreateCinemaDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid cinema: no errors")
        void shouldReturnNoErrorsForValidCinema() {
            assertThat(validator.validate(validDto())).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dt object", "is null"));
        }

        @Test
        @DisplayName("Blank city: city error")
        void shouldReturnErrorWhenCityIsBlank() {
            CreateCinemaDto dto = CreateCinemaDto.builder()
                    .city(" ")
                    .street("Long Street")
                    .cinemaHallsCapacity(List.of(hall(5, 5)))
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("City name", "is blank");
        }

        @Test
        @DisplayName("Missing hall capacity list: capacity error")
        void shouldReturnErrorWhenCinemaHallsCapacityIsNull() {
            CreateCinemaDto dto = CreateCinemaDto.builder()
                    .city("Warsaw")
                    .street("Long Street")
                    .cinemaHallsCapacity(null)
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("Cinema halls capacity", "are required");
        }

        @Test
        @DisplayName("Null hall in list: indexed hall error")
        void shouldReturnErrorWhenHallCapacityItemIsNull() {
            CreateCinemaDto dto = CreateCinemaDto.builder()
                    .city("Warsaw")
                    .street("Long Street")
                    .cinemaHallsCapacity(Arrays.asList(hall(5, 5), null))
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry("cinemaHall no. 2", "is null");
        }

        @Test
        @DisplayName("Non-positive hall capacity: indexed hall error with row and column")
        void shouldReturnErrorWhenHallCapacityHasNonPositiveValues() {
            CreateCinemaDto dto = CreateCinemaDto.builder()
                    .city("Warsaw")
                    .street("Long Street")
                    .cinemaHallsCapacity(List.of(hall(0, -1)))
                    .build();

            assertThat(validator.validate(dto))
                    .containsEntry(
                            "cinemaHall no. 1",
                            "cinema hall row and col numbers must be positive integer, actual values are: row: 0, col: -1"
                    );
        }
    }

    private CreateCinemaDto validDto() {
        return CreateCinemaDto.builder()
                .city("Warsaw")
                .street("Long Street")
                .cinemaHallsCapacity(List.of(hall(5, 5), hall(6, 7)))
                .build();
    }

    private CreateCinemaHallDto hall(Integer rows, Integer columns) {
        return CreateCinemaHallDto.builder()
                .rowNo(rows)
                .colNo(columns)
                .build();
    }
}
