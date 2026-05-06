package com.rzodeczko.domain.vo;

import com.rzodeczko.domain.exception.DiscountException;

import java.math.BigDecimal;

public class Discount {
    private BigDecimal value;

    private Discount() {
        this.value = BigDecimal.ZERO;
    }

    private Discount(String value) {
        this.value = init(value);
    }

    private Discount(BigDecimal value) {
        this.value = value;
    }

    public static Discount of(BigDecimal value) {
        return new Discount(value);
    }

    public static Discount of(String value) {
        return new Discount(value);
    }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public Discount inverse() {
        return new Discount(BigDecimal.ONE.subtract(value));
    }

    private static BigDecimal init(String value) {
        if (value == null || !value.matches("\\d\\.\\d+")) {
            throw new DiscountException("discount value is not correct");
        }

        var decimalValue = new BigDecimal(value);
        if (decimalValue.compareTo(BigDecimal.ZERO) < 0 || decimalValue.compareTo(BigDecimal.ONE) > 0) {
            throw new DiscountException("discount value is out of range");
        }

        return decimalValue;
    }

    public Discount add(Discount toAdd) {
        return Discount.of(this.getValue().add(toAdd.getValue()));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
