package com.rzodeczko.domain.vo;

import com.rzodeczko.domain.exception.DiscountException;

import java.math.BigDecimal;

import static java.util.Objects.isNull;

public class Money {

    private BigDecimal value;

    private Money() {
        this.value = BigDecimal.ZERO;
    }

    private Money(String value) {
        this.value = init(value);
    }

    private Money(BigDecimal value) {
        this.value = value;
    }

    public static Money of(String value) {
        return new Money(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Money add(String value) {
        return new Money(this.value.add(init(value)));
    }

    public Money add(Money money) {
        return new Money(this.value.add(money.value));
    }

    public Money multiply(Integer quantity) {
        return new Money(this.value.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money multiply(String value) {
        return new Money(this.value.multiply(init(value)));
    }

    private static BigDecimal init(String value) {
        if (isNull(value) || !value.matches("\\d+(\\.\\d+)?")) {
            throw new DiscountException("Money value is not correct");
        }
        return new BigDecimal(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
