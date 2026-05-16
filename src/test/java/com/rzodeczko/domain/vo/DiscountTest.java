package com.rzodeczko.domain.vo;

import com.rzodeczko.domain.exception.DiscountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Discount")
class DiscountTest {

    @Nested
    @DisplayName("Constructor (BigDecimal)")
    class ConstructorTests {

        @Test
        @DisplayName("Valid value (0.0 <= x <= 1.0): creates Discount")
        void shouldCreateDiscountFromValidBigDecimal() {
            Discount discount = new Discount(BigDecimal.valueOf(0.5));

            assertThat(discount.getValue()).isEqualTo(BigDecimal.valueOf(0.5));
        }

        @Test
        @DisplayName("Zero value: creates Discount")
        void shouldCreateDiscountWithZero() {
            Discount discount = new Discount(BigDecimal.ZERO);

            assertThat(discount.getValue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("One value: creates Discount")
        void shouldCreateDiscountWithOne() {
            Discount discount = new Discount(BigDecimal.ONE);

            assertThat(discount.getValue()).isEqualTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("Null value: DiscountException")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> new Discount(null))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is out of range");
        }

        @Test
        @DisplayName("Negative value: DiscountException")
        void shouldThrowWhenValueIsNegative() {
            assertThatThrownBy(() -> new Discount(BigDecimal.valueOf(-0.1)))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is out of range");
        }

        @Test
        @DisplayName("Value > 1.0: DiscountException")
        void shouldThrowWhenValueIsGreaterThanOne() {
            assertThatThrownBy(() -> new Discount(BigDecimal.valueOf(1.5)))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is out of range");
        }
    }

    @Nested
    @DisplayName("of(BigDecimal)")
    class OfBigDecimalTests {

        @Test
        @DisplayName("Valid value: creates Discount")
        void shouldCreateDiscountFromValidBigDecimal() {
            Discount discount = Discount.of(BigDecimal.valueOf(0.25));

            assertThat(discount.getValue()).isEqualTo(BigDecimal.valueOf(0.25));
        }
    }

    @Nested
    @DisplayName("of(String)")
    class OfStringTests {

        @Test
        @DisplayName("Valid decimal string (0.X): creates Discount")
        void shouldCreateDiscountFromValidString() {
            Discount discount = Discount.of("0.50");

            assertThat(discount.getValue()).isEqualByComparingTo(BigDecimal.valueOf(0.50));
        }

        @Test
        @DisplayName("Invalid format (no leading zero): DiscountException")
        void shouldThrowWhenStringFormatIsInvalid() {
            assertThatThrownBy(() -> Discount.of(".5"))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is not correct");
        }

        @Test
        @DisplayName("Value out of range: DiscountException")
        void shouldThrowWhenValueIsOutOfRange() {
            assertThatThrownBy(() -> Discount.of("1.5"))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is out of range");
        }

        @Test
        @DisplayName("Null string: DiscountException")
        void shouldThrowWhenStringIsNull() {
            assertThatThrownBy(() -> Discount.of((String) null))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is not correct");
        }
    }

    @Nested
    @DisplayName("inverse()")
    class InverseTests {

        @Test
        @DisplayName("inverse of 0.0 = 1.0")
        void shouldInverseZero() {
            Discount discount = new Discount(BigDecimal.ZERO);

            Discount inversed = discount.inverse();

            assertThat(inversed.getValue()).isEqualTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("inverse of 1.0 = 0.0")
        void shouldInverseOne() {
            Discount discount = new Discount(BigDecimal.ONE);

            Discount inversed = discount.inverse();

            assertThat(inversed.getValue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("inverse of 0.3 = 0.7")
        void shouldInverseMiddleValue() {
            Discount discount = new Discount(BigDecimal.valueOf(0.3));

            Discount inversed = discount.inverse();

            assertThat(inversed.getValue()).isEqualTo(BigDecimal.valueOf(0.7));
        }
    }

    @Nested
    @DisplayName("add(Discount)")
    class AddTests {

        @Test
        @DisplayName("Valid discounts: adds values")
        void shouldAddValidDiscounts() {
            Discount discount1 = new Discount(BigDecimal.valueOf(0.2));
            Discount discount2 = new Discount(BigDecimal.valueOf(0.3));

            Discount result = discount1.add(discount2);

            assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(0.5));
        }

        @Test
        @DisplayName("Adding results in value > 1.0: DiscountException")
        void shouldThrowWhenSumExceedsOne() {
            Discount discount1 = new Discount(BigDecimal.valueOf(0.7));
            Discount discount2 = new Discount(BigDecimal.valueOf(0.5));

            assertThatThrownBy(() -> discount1.add(discount2))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("discount value is out of range");
        }

        @Test
        @DisplayName("Adding zero: returns same value")
        void shouldAddZero() {
            Discount discount1 = new Discount(BigDecimal.valueOf(0.5));
            Discount discount2 = new Discount(BigDecimal.ZERO);

            Discount result = discount1.add(discount2);

            assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(0.5));
        }
    }

    @Nested
    @DisplayName("setValue(BigDecimal)")
    class SetValueTests {

        @Test
        @DisplayName("Valid value: creates new Discount with new value")
        void shouldSetNewValue() {
            Discount discount = new Discount(BigDecimal.valueOf(0.2));

            Discount updated = discount.setValue(BigDecimal.valueOf(0.8));

            assertThat(updated.getValue()).isEqualTo(BigDecimal.valueOf(0.8));
            assertThat(discount.getValue()).isEqualTo(BigDecimal.valueOf(0.2)); // Original unchanged
        }

        @Test
        @DisplayName("Invalid value: DiscountException")
        void shouldThrowWhenSettingInvalidValue() {
            Discount discount = new Discount(BigDecimal.valueOf(0.5));

            assertThatThrownBy(() -> discount.setValue(BigDecimal.valueOf(1.5)))
                    .isInstanceOf(DiscountException.class);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("Returns string representation")
        void shouldReturnStringRepresentation() {
            Discount discount = new Discount(BigDecimal.valueOf(0.25));

            String result = discount.toString();

            assertThat(result).isEqualTo("0.25");
        }
    }

    @Nested
    @DisplayName("No-arg constructor")
    class NoArgConstructorTests {

        @Test
        @DisplayName("Creates Discount with ZERO value")
        void shouldCreateDiscountWithZero() {
            Discount discount = new Discount();

            assertThat(discount.getValue()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getValue()")
    class GetValueTests {

        @Test
        @DisplayName("Returns internal value")
        void shouldReturnValue() {
            Discount discount = new Discount(BigDecimal.valueOf(0.42));

            assertThat(discount.getValue()).isEqualTo(BigDecimal.valueOf(0.42));
        }
    }
}

