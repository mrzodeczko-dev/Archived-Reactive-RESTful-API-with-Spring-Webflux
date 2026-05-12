package it.handlers;

import com.rzodeczko.application.dto.CreateTicketOrderDto;
import com.rzodeczko.application.dto.TicketOrderDto;
import com.rzodeczko.application.service.TicketOrderService;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.presentation.routing.TicketOrdersRouting;
import com.rzodeczko.presentation.routing.handlers.TicketOrderHandler;
import com.rzodeczko.presentation.routing.userprovider.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest
@Import({
        TicketOrdersRouting.class,
        TicketOrderHandler.class,
        AbstractHandlerSliceTest.Configs.class
})
@ActiveProfiles("handlers")
class TicketOrderHandlerSliceTest {

    @Autowired
    ApplicationContext context;

    private WebTestClient client;

    @MockitoBean
    private TicketOrderService ticketOrderService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    private static TicketOrderDto sampleOrder(String id, String username) {
        return TicketOrderDto.builder()
                .id(id)
                .username(username)
                .tickets(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("POST /ticketOrders → 201 + saved TicketOrderDto")
    void shouldOrderTickets() {
        TicketOrderDto saved = sampleOrder("o-1", "user1");
        when(ticketOrderService.addTicketOrder(any(), any(CreateTicketOrderDto.class)))
                .thenReturn(Mono.just(saved));

        client.post().uri("/ticketOrders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateTicketOrderDto.builder()
                        .movieEmissionId("e-1")
                        .ticketGroupType(TicketGroupType.NORMAL)
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("o-1")
                .jsonPath("$.username").isEqualTo("user1");
    }

    @Test
    @DisplayName("PUT /ticketsOrders/cancel/orderId/{orderId} → 200 + cancelled order")
    void shouldCancelOrder() {
        TicketOrderDto cancelled = sampleOrder("o-1", "user1");
        when(currentUserProvider.username()).thenReturn(Mono.just("user1"));
        when(ticketOrderService.cancelOrder("user1", "o-1")).thenReturn(Mono.just(cancelled));

        client.put().uri("/ticketsOrders/cancel/orderId/o-1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("o-1");
    }

    @Test
    @DisplayName("GET /ticketsOrders/username → 200 + list of orders for logged user")
    void shouldGetAllOrdersForLoggedUser() {
        when(currentUserProvider.username()).thenReturn(Mono.just("user1"));
        when(ticketOrderService.getAllTicketOrdersForLoggedUser("user1")).thenReturn(Flux.just(
                sampleOrder("o-1", "user1"),
                sampleOrder("o-2", "user1")));

        client.get().uri("/ticketsOrders/username")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].username").isEqualTo("user1");
    }

    @Test
    @DisplayName("GET /ticketsOrders/username → 200 + empty list when no orders")
    void shouldReturnEmptyListWhenNoOrders() {
        when(currentUserProvider.username()).thenReturn(Mono.just("user1"));
        when(ticketOrderService.getAllTicketOrdersForLoggedUser("user1")).thenReturn(Flux.empty());

        client.get().uri("/ticketsOrders/username")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }
}