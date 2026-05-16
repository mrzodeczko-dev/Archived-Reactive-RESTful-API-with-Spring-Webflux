package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.AddCinemaHallToCinemaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AddCinemaHallToCinemaDtoValidatorTest {

    private final AddCinemaHallToCinemaDtoValidator validator = new AddCinemaHallToCinemaDtoValidator();

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Valid DTO: no errors")
        void shouldReturnNoErrorsForValidDto() {
            AddCinemaHallToCinemaDto dto = new AddCinemaHallToCinemaDto(5, 5, "cinema-1");

            assertThat(validator.validate(dto)).isEmpty();
        }

        @Test
        @DisplayName("Null DTO: dto object error only")
        void shouldReturnErrorWhenDtoIsNull() {
            assertThat(validator.validate(null))
                    .containsExactly(Map.entry("dto object", "is null"));
        }

        @Test
        @DisplayName("Missing cinema id: cinema id error")
        void shouldReturnErrorWhenCinemaIdIsNull() {
            AddCinemaHallToCinemaDto dto = new AddCinemaHallToCinemaDto(5, 5, null);

            assertThat(validator.validate(dto))
                    .containsEntry("cinema id", "is null");
        }
    }
}
