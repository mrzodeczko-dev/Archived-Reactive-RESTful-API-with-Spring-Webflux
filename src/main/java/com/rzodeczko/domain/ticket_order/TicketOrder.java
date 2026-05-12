package com.rzodeczko.domain.ticket_order;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
import com.rzodeczko.domain.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record TicketOrder(
        String id,
        User user,
        LocalDate orderDate,
        TicketOrderStatus ticketOrderStatus,
        MovieEmission movieEmission,
        List<Ticket> tickets,
        TicketGroupType ticketGroupType
) implements GenericEntity {

    public TicketOrder {
        tickets = tickets == null ? new ArrayList<>() : new ArrayList<>(tickets);
    }

    public TicketOrder() {
        this(null, null, null, null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id).user(user).orderDate(orderDate).ticketOrderStatus(ticketOrderStatus)
                .movieEmission(movieEmission).tickets(tickets).ticketGroupType(ticketGroupType);
    }

    public String getId() { return id; }
    public TicketOrder setId(String id) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    public User getUser() { return user; }
    public TicketOrder setUser(User user) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    public LocalDate getOrderDate() { return orderDate; }
    public TicketOrder setOrderDate(LocalDate orderDate) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    public TicketOrderStatus getTicketOrderStatus() { return ticketOrderStatus; }
    public TicketOrder setTicketOrderStatus(TicketOrderStatus ticketOrderStatus) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    public MovieEmission getMovieEmission() { return movieEmission; }
    public TicketOrder setMovieEmission(MovieEmission movieEmission) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    public List<Ticket> getTickets() { return tickets; }
    public TicketOrder setTickets(List<Ticket> tickets) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    public TicketGroupType getTicketGroupType() { return ticketGroupType; }
    public TicketOrder setTicketGroupType(TicketGroupType ticketGroupType) { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }

    public TicketPurchase toTicketPurchase() {
        return TicketPurchase.builder()
                .purchaseDate(LocalDate.now())
                .ticketGroupType(ticketGroupType)
                .movieEmission(movieEmission)
                .tickets(purchasedTickets())
                .user(user)
                .build();
    }

    public TicketOrder changeOrderStatusToDone() {
        return setTicketOrderStatus(TicketOrderStatus.DONE).setTickets(purchasedTickets());
    }

    public TicketOrder changeOrderStatusToCancelled() {
        return setTicketOrderStatus(TicketOrderStatus.CANCELLED);
    }

    private List<Ticket> purchasedTickets() {
        return tickets == null ? List.of() : tickets.stream()
                .map(ticket -> ticket.setTicketStatus(TicketStatus.PURCHASED))
                .toList();
    }

    public static class Builder {
        private String id;
        private User user;
        private LocalDate orderDate;
        private TicketOrderStatus ticketOrderStatus;
        private MovieEmission movieEmission;
        private List<Ticket> tickets;
        private TicketGroupType ticketGroupType;

        public Builder id(String id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder orderDate(LocalDate orderDate) { this.orderDate = orderDate; return this; }
        public Builder ticketOrderStatus(TicketOrderStatus s) { this.ticketOrderStatus = s; return this; }
        public Builder movieEmission(MovieEmission me) { this.movieEmission = me; return this; }
        public Builder tickets(List<Ticket> tickets) { this.tickets = tickets; return this; }
        public Builder ticketGroupType(TicketGroupType t) { this.ticketGroupType = t; return this; }
        public TicketOrder build() { return new TicketOrder(id, user, orderDate, ticketOrderStatus, movieEmission, tickets, ticketGroupType); }
    }
}
