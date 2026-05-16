package com.rzodeczko.domain.vo;

import com.rzodeczko.domain.exception.DiscountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money")
class MoneyTest {

    @Nested
    @DisplayName("Constructor (BigDecimal)")
    class ConstructorTests {

        @Test
        @DisplayName("Valid BigDecimal: creates Money")
        void shouldCreateMoneyFromValidBigDecimal() {
            Money money = new Money(BigDecimal.TEN);

            assertThat(money.getValue()).isEqualTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("Zero BigDecimal: creates Money")
        void shouldCreateMoneyFromZero() {
            Money money = new Money(BigDecimal.ZERO);

            assertThat(money.getValue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Null BigDecimal: DiscountException")
        void shouldThrowWhenBigDecimalIsNull() {
            assertThatThrownBy(() -> new Money(null))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money value is not correct");
        }

        @Test
        @DisplayName("Negative BigDecimal: DiscountException")
        void shouldThrowWhenBigDecimalIsNegative() {
            assertThatThrownBy(() -> new Money(BigDecimal.valueOf(-5)))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money value is not correct");
        }
    }

    @Nested
    @DisplayName("of(String)")
    class OfStringTests {

        @Test
        @DisplayName("Valid numeric string: creates Money")
        void shouldCreateMoneyFromValidString() {
            Money money = Money.of("19.99");

            assertThat(money.getValue()).isEqualTo(BigDecimal.valueOf(19.99));
        }

        @Test
        @DisplayName("Integer string: creates Money")
        void shouldCreateMoneyFromIntegerString() {
            Money money = Money.of("100");

            assertThat(money.getValue()).isEqualTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("Invalid format (letters): DiscountException")
        void shouldThrowWhenStringIsInvalid() {
            assertThatThrownBy(() -> Money.of("abc"))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money value is not correct");
        }

        @Test
        @DisplayName("Null string: DiscountException")
        void shouldThrowWhenStringIsNull() {
            assertThatThrownBy(() -> Money.of(null))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money value is not correct");
        }

        @Test
        @DisplayName("Empty string: DiscountException")
        void shouldThrowWhenStringIsEmpty() {
            assertThatThrownBy(() -> Money.of(""))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money value is not correct");
        }
    }

    @Nested
    @DisplayName("add(String)")
    class AddStringTests {

        @Test
        @DisplayName("Valid string: adds amounts")
        void shouldAddValidString() {
            Money money = new Money(BigDecimal.TEN);
            Money result = money.add("5.50");

            assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(15.50));
        }

        @Test
        @DisplayName("Invalid format: DiscountException")
        void shouldThrowWhenAddingInvalidString() {
            Money money = new Money(BigDecimal.TEN);

            assertThatThrownBy(() -> money.add("invalid"))
                    .isInstanceOf(DiscountException.class);
        }
    }

    @Nested
    @DisplayName("add(Money)")
    class AddMoneyTests {

        @Test
        @DisplayName("Valid Money: adds amounts")
        void shouldAddValidMoney() {
            Money money1 = new Money(BigDecimal.TEN);
            Money money2 = new Money(BigDecimal.valueOf(5.50));

            Money result = money1.add(money2);

            assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(15.50));
        }

        @Test
        @DisplayName("Zero Money: adds zero")
        void shouldAddZeroMoney() {
            Money money1 = new Money(BigDecimal.TEN);
            Money money2 = new Money(BigDecimal.ZERO);

            Money result = money1.add(money2);

            assertThat(result.getValue()).isEqualTo(BigDecimal.TEN);
        }
    }

    @Nested
    @DisplayName("multiply(Integer)")
    class MultiplyIntegerTests {

        @Test
        @DisplayName("Positive quantity: multiplies amount")
        void shouldMultiplyByPositiveQuantity() {
            Money money = new Money(BigDecimal.TEN);

            Money result = money.multiply(3);

            assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(30));
        }

        @Test
        @DisplayName("Zero quantity: results in zero")
        void shouldMultiplyByZero() {
            Money money = new Money(BigDecimal.TEN);

            Money result = money.multiply(0);

            assertThat(result.getValue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Negative quantity: DiscountException")
        void shouldThrowWhenQuantityIsNegative() {
            Money money = new Money(BigDecimal.TEN);

            assertThatThrownBy(() -> money.multiply(-5))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money multiplier is not correct");
        }

        @Test
        @DisplayName("Null quantity: DiscountException")
        void shouldThrowWhenQuantityIsNull() {
            Money money = new Money(BigDecimal.TEN);

            assertThatThrownBy(() -> money.multiply((Integer) null))
                    .isInstanceOf(DiscountException.class)
                    .hasMessage("Money multiplier is not correct");
        }
    }

    @Nested
    @DisplayName("multiply(String)")
    class MultiplyStringTests {

        @Test
        @DisplayName("Valid multiplier string: multiplies amount")
        void shouldMultiplyByValidString() {
            Money money = new Money(BigDecimal.TEN);

            Money result = money.multiply("1.5");

            assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(15));
        }

        @Test
        @DisplayName("Invalid multiplier: DiscountException")
        void shouldThrowWhenMultiplierIsInvalid() {
            Money money = new Money(BigDecimal.TEN);

            assertThatThrownBy(() -> money.multiply("invalid"))
                    .isInstanceOf(DiscountException.class);
        }
    }

    @Nested
    @DisplayName("setValue(BigDecimal)")
    class SetValueTests {

        @Test
        @DisplayName("Valid value: creates new Money with new value")
        void shouldSetNewValue() {
            Money money = new Money(BigDecimal.TEN);

            Money updated = money.setValue(BigDecimal.valueOf(25));

            assertThat(updated.getValue()).isEqualTo(BigDecimal.valueOf(25));
            assertThat(money.getValue()).isEqualTo(BigDecimal.TEN); // Original unchanged
        }

        @Test
        @DisplayName("Negative value: DiscountException")
        void shouldThrowWhenSettingNegativeValue() {
            Money money = new Money(BigDecimal.TEN);

            assertThatThrownBy(() -> money.setValue(BigDecimal.valueOf(-10)))
                    .isInstanceOf(DiscountException.class);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("Returns string representation")
        void shouldReturnStringRepresentation() {
            Money money = new Money(BigDecimal.valueOf(19.99));

            String result = money.toString();

            assertThat(result).isEqualTo("19.99");
        }
    }

    @Nested
    @DisplayName("No-arg constructor")
    class NoArgConstructorTests {

        @Test
        @DisplayName("Creates Money with ZERO value")
        void shouldCreateMoneyWithZero() {
            Money money = new Money();

            assertThat(money.getValue()).isEqualTo(BigDecimal.ZERO);
        }
    }
}

