package com.rzodeczko.application.validator.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validations")
class ValidationsTest {

    @Nested
    @DisplayName("createErrorMessage(Map)")
    class CreateErrorMessageTests {

        @Test
        @DisplayName("Empty map: returns empty string")
        void shouldReturnEmptyStringForEmptyMap() {
            Map<String, String> errors = new HashMap<>();

            String result = Validations.createErrorMessage(errors);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Map with string values: formats message")
        void shouldFormatStringValues() {
            Map<String, String> errors = new HashMap<>();
            errors.put("field1", "error1");
            errors.put("field2", "error2");

            String result = Validations.createErrorMessage(errors);

            assertThat(result).contains("field1 -> error1");
            assertThat(result).contains("field2 -> error2");
            assertThat(result).contains(",");
        }

        @Test
        @DisplayName("Map with single string error: formats correctly")
        void shouldHandleSingleError() {
            Map<String, String> errors = Map.of("username", "must not be blank");

            String result = Validations.createErrorMessage(errors);

            assertThat(result).isEqualTo("username -> must not be blank");
        }

        @Test
        @DisplayName("Map with list of entries: formats list errors")
        void shouldFormatListErrors() {
            Map<String, List<?>> errors = new HashMap<>();
            List<Map.Entry<String, String>> entries = List.of(
                    new AbstractMap.SimpleEntry<>("field1", "error1"),
                    new AbstractMap.SimpleEntry<>("field2", "error2")
            );
            errors.put("0", entries);

            String result = Validations.createErrorMessage(errors);

            assertThat(result).contains("Item no. 0");
            assertThat(result).contains("field1 -> error1");
            assertThat(result).contains("field2 -> error2");
        }

        @Test
        @DisplayName("Map with mixed types: handles correctly")
        void shouldHandleMixedTypes() {
            Map<String, Object> errors = new HashMap<>();
            errors.put("string_field", "string error");
            errors.put("list_field", List.of(
                    new AbstractMap.SimpleEntry<>("item", "list error")
            ));

            String result = Validations.createErrorMessage(errors);

            assertThat(result).contains("string_field -> string error");
            assertThat(result).contains("Item no. list_field");
            assertThat(result).contains("item -> list error");
        }

        @Test
        @DisplayName("Map with null list: handles gracefully")
        void shouldHandleNullListValues() {
            Map<String, List<?>> errors = new HashMap<>();
            List<Object> nullList = new ArrayList<>();
            nullList.add(null);
            nullList.add(null);
            errors.put("field", nullList);

            String result = Validations.createErrorMessage(errors);

            assertThat(result).contains("Item no. field");
        }

        @Test
        @DisplayName("Map with invalid type: returns empty string for that entry")
        void shouldHandleInvalidTypes() {
            Map<String, Object> errors = new HashMap<>();
            errors.put("number_field", 123);

            String result = Validations.createErrorMessage(errors);

            // Invalid type returns empty string, so the result depends on joining behavior
            assertThat(result).doesNotContain("123");
        }
    }

    @Nested
    @DisplayName("hasErrors(Map)")
    class HasErrorsTests {

        @Test
        @DisplayName("Empty map: returns false")
        void shouldReturnFalseForEmptyMap() {
            Map<String, String> errors = new HashMap<>();

            boolean result = Validations.hasErrors(errors);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Non-empty map: returns true")
        void shouldReturnTrueForNonEmptyMap() {
            Map<String, String> errors = Map.of("field", "error");

            boolean result = Validations.hasErrors(errors);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Map with multiple entries: returns true")
        void shouldReturnTrueForMultipleErrors() {
            Map<String, String> errors = new HashMap<>();
            errors.put("field1", "error1");
            errors.put("field2", "error2");
            errors.put("field3", "error3");

            boolean result = Validations.hasErrors(errors);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Map with null values: returns true")
        void shouldReturnTrueWithNullValues() {
            Map<String, String> errors = new HashMap<>();
            errors.put("field", null);

            boolean result = Validations.hasErrors(errors);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Complex scenarios")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Multiple string errors with special characters")
        void shouldHandleSpecialCharacters() {
            Map<String, String> errors = Map.of(
                    "email", "must be a valid email (format: user@example.com)",
                    "phone", "must contain 9-15 digits"
            );

            String result = Validations.createErrorMessage(errors);

            assertThat(result).contains("email -> must be a valid email");
            assertThat(result).contains("phone -> must contain 9-15 digits");
        }

        @Test
        @DisplayName("Large list of errors: formats all")
        void shouldHandlargeErrorList() {
            Map<String, List<?>> errors = new HashMap<>();
            List<Map.Entry<String, String>> items = List.of(
                    new AbstractMap.SimpleEntry<>("row_0_col", "required"),
                    new AbstractMap.SimpleEntry<>("row_1_price", "must be positive"),
                    new AbstractMap.SimpleEntry<>("row_2_date", "invalid format")
            );
            errors.put("10", items);

            String result = Validations.createErrorMessage(errors);

            assertThat(result).contains("Item no. 10");
            assertThat(result).contains("row_0_col -> required");
            assertThat(result).contains("row_1_price -> must be positive");
            assertThat(result).contains("row_2_date -> invalid format");
        }
    }
}

