package com.rzodeczko.domain.ticket_order;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;

import java.time.LocalDate;
import java.util.List;

public class TicketOrder implements GenericEntity {

    private String id;
    private User user;
    private LocalDate orderDate;
    private TicketOrderStatus ticketOrderStatus;
    private MovieEmission movieEmission;
    private List<Ticket> tickets;
    private TicketGroupType ticketGroupType;

    public TicketOrder() {
    }

    public TicketOrder(String id, User user, LocalDate orderDate, TicketOrderStatus ticketOrderStatus,
                       MovieEmission movieEmission, List<Ticket> tickets, TicketGroupType ticketGroupType) {
        this.id = id;
        this.user = user;
        this.orderDate = orderDate;
        this.ticketOrderStatus = ticketOrderStatus;
        this.movieEmission = movieEmission;
        this.tickets = tickets;
        this.ticketGroupType = ticketGroupType;
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
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public TicketOrderStatus getTicketOrderStatus() { return ticketOrderStatus; }
    public void setTicketOrderStatus(TicketOrderStatus ticketOrderStatus) { this.ticketOrderStatus = ticketOrderStatus; }
    public MovieEmission getMovieEmission() { return movieEmission; }
    public void setMovieEmission(MovieEmission movieEmission) { this.movieEmission = movieEmission; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
    public TicketGroupType getTicketGroupType() { return ticketGroupType; }
    public void setTicketGroupType(TicketGroupType ticketGroupType) { this.ticketGroupType = ticketGroupType; }

    public TicketPurchase toTicketPurchase() {
        return TicketPurchase.builder()
                .purchaseDate(LocalDate.now())
                .ticketGroupType(ticketGroupType)
                .movieEmission(movieEmission)
                .tickets(tickets)
                .user(user)
                .build();
    }

    public TicketOrder changeOrderStatusToDone() {
        ticketOrderStatus = TicketOrderStatus.DONE;
        return this;
    }

    public TicketOrder changeOrderStatusToCancelled() {
        ticketOrderStatus = TicketOrderStatus.CANCELLED;
        return this;
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
