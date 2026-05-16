package com.rzodeczko.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DiscountException")
class DiscountExceptionTest {

    @Test
    @DisplayName("Creates exception with message")
    void shouldCreateExceptionWithMessage() {
        String message = "Invalid discount value";

        DiscountException exception = new DiscountException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Extends RuntimeException")
    void shouldExtendRuntimeException() {
        DiscountException exception = new DiscountException("test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Can be caught as RuntimeException")
    void shouldBeCatchableAsRuntimeException() {
        RuntimeException exception = new DiscountException("test message");

        assertThat(exception).isInstanceOf(DiscountException.class);
        assertThat(exception.getMessage()).isEqualTo("test message");
    }

    @Test
    @DisplayName("Null message: creates exception")
    void shouldHandleNullMessage() {
        DiscountException exception = new DiscountException(null);

        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Empty message: creates exception")
    void shouldHandleEmptyMessage() {
        DiscountException exception = new DiscountException("");

        assertThat(exception.getMessage()).isEmpty();
    }

    @Test
    @DisplayName("Message with special characters")
    void shouldHandleSpecialCharactersInMessage() {
        String message = "Discount value must be between 0.0 and 1.0 [0-1]";

        DiscountException exception = new DiscountException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Can be thrown and caught")
    void shouldBeThrowableAndCatchable() {
        try {
            throw new DiscountException("Test error");
        } catch (DiscountException e) {
            assertThat(e.getMessage()).isEqualTo("Test error");
        }
    }

    @Test
    @DisplayName("Stack trace is populated")
    void shouldHaveStackTrace() {
        DiscountException exception = new DiscountException("test");

        assertThat(exception.getStackTrace()).isNotEmpty();
    }
}

