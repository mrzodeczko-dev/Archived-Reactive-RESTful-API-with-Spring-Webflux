package com.rzodeczko.domain.ticket_order.enums;

import com.rzodeczko.domain.vo.Discount;

public enum TicketGroupType {
    FAMILY(Discount.of("0.2")),
    NORMAL(Discount.of("0.0"));

    private final Discount discount;

    TicketGroupType(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }
}
