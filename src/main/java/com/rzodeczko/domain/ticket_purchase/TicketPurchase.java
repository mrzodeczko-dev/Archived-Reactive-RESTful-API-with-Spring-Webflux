package com.rzodeczko.domain.ticket_purchase;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record TicketPurchase(
        String id,
        User user,
        LocalDate purchaseDate,
        MovieEmission movieEmission,
        List<Ticket> tickets,
        TicketGroupType ticketGroupType
) implements GenericEntity {

    public TicketPurchase {
        tickets = tickets == null ? new ArrayList<>() : new ArrayList<>(tickets);
    }

    public TicketPurchase() {
        this(null, null, null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public TicketPurchase setId(String id) { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }
    public User getUser() { return user; }
    public TicketPurchase setUser(User user) { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public TicketPurchase setPurchaseDate(LocalDate purchaseDate) { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }
    public MovieEmission getMovieEmission() { return movieEmission; }
    public TicketPurchase setMovieEmission(MovieEmission movieEmission) { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }
    public List<Ticket> getTickets() { return tickets; }
    public TicketPurchase setTickets(List<Ticket> tickets) { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }
    public TicketGroupType getTicketGroupType() { return ticketGroupType; }
    public TicketPurchase setTicketGroupType(TicketGroupType ticketGroupType) { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }

    public static class Builder {
        private String id;
        private User user;
        private LocalDate purchaseDate;
        private MovieEmission movieEmission;
        private List<Ticket> tickets;
        private TicketGroupType ticketGroupType;

        public Builder id(String id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder purchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; return this; }
        public Builder movieEmission(MovieEmission me) { this.movieEmission = me; return this; }
        public Builder tickets(List<Ticket> tickets) { this.tickets = tickets; return this; }
        public Builder ticketGroupType(TicketGroupType t) { this.ticketGroupType = t; return this; }
        public TicketPurchase build() { return new TicketPurchase(id, user, purchaseDate, movieEmission, tickets, ticketGroupType); }
    }
}
