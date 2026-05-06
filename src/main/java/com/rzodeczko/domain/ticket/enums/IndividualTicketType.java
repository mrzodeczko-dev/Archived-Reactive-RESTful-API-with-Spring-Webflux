package com.rzodeczko.domain.ticket.enums;

import com.rzodeczko.domain.vo.Discount;

public enum IndividualTicketType {
    REGULAR(Discount.of("0.0")),
    STUDENT(Discount.of("0.2"));

    private final Discount discount;

    IndividualTicketType(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }
}
