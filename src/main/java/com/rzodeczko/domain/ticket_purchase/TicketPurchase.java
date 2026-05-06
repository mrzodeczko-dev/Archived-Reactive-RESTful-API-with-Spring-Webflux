package com.rzodeczko.domain.ticket_purchase;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;

import java.time.LocalDate;
import java.util.List;

public class TicketPurchase implements GenericEntity {

    private String id;
    private User user;
    private LocalDate purchaseDate;
    private MovieEmission movieEmission;
    private List<Ticket> tickets;
    private TicketGroupType ticketGroupType;

    public TicketPurchase() {
    }

    public TicketPurchase(String id, User user, LocalDate purchaseDate, MovieEmission movieEmission,
                          List<Ticket> tickets, TicketGroupType ticketGroupType) {
        this.id = id;
        this.user = user;
        this.purchaseDate = purchaseDate;
        this.movieEmission = movieEmission;
        this.tickets = tickets;
        this.ticketGroupType = ticketGroupType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public MovieEmission getMovieEmission() { return movieEmission; }
    public void setMovieEmission(MovieEmission movieEmission) { this.movieEmission = movieEmission; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
    public TicketGroupType getTicketGroupType() { return ticketGroupType; }
    public void setTicketGroupType(TicketGroupType ticketGroupType) { this.ticketGroupType = ticketGroupType; }

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
