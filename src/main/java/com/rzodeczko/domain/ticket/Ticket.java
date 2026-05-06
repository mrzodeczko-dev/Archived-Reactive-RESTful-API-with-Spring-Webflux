package com.rzodeczko.domain.ticket;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;

public class Ticket implements GenericEntity {

    private String id;
    private TicketStatus ticketStatus;
    private IndividualTicketType type;
    private Position position;
    private Discount discount;
    private Money price;

    public Ticket() {
    }

    public Ticket(String id, TicketStatus ticketStatus, IndividualTicketType type,
                  Position position, Discount discount, Money price) {
        this.id = id;
        this.ticketStatus = ticketStatus;
        this.type = type;
        this.position = position;
        this.discount = discount;
        this.price = price;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TicketStatus getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(TicketStatus ticketStatus) { this.ticketStatus = ticketStatus; }
    public IndividualTicketType getType() { return type; }
    public void setType(IndividualTicketType type) { this.type = type; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public Discount getDiscount() { return discount; }
    public void setDiscount(Discount discount) { this.discount = discount; }
    public Money getPrice() { return price; }
    public void setPrice(Money price) { this.price = price; }

    public static class Builder {
        private String id;
        private TicketStatus ticketStatus;
        private IndividualTicketType type;
        private Position position;
        private Discount discount;
        private Money price;

        public Builder id(String id) { this.id = id; return this; }
        public Builder ticketStatus(TicketStatus ticketStatus) { this.ticketStatus = ticketStatus; return this; }
        public Builder type(IndividualTicketType type) { this.type = type; return this; }
        public Builder position(Position position) { this.position = position; return this; }
        public Builder discount(Discount discount) { this.discount = discount; return this; }
        public Builder price(Money price) { this.price = price; return this; }
        public Ticket build() { return new Ticket(id, ticketStatus, type, position, discount, price); }
    }
}
