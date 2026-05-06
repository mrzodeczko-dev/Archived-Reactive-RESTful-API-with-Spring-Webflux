package com.rzodeczko.application.service;

import com.rzodeczko.application.exception.TicketOrderServiceException;
import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.application.port.out.TicketOrderPort;
import com.rzodeczko.application.port.out.TicketPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.service.TicketOrderService;
import com.rzodeczko.application.validator.CreateTicketsOrderDtoValidator;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketOrderServiceTest {

    @Mock
    private TicketOrderPort ticketOrderRepository;
    @Mock
    private TicketPort ticketRepository;
    @Mock
    private UserPort userRepository;
    @Mock
    private MovieEmissionPort movieEmissionRepository;
    @Mock
    private CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator;
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private TicketOrderService ticketOrderService;

    private TicketOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = TicketOrder.builder()
                .id("order-1")
                .user(User.builder()
                        .username("test@example.com")
                        .build())
                .movieEmission(MovieEmission.builder()
                        .baseTicketPrice(Money.of("50"))
                        .movie(Movie.builder()
                                .name("Sample Movie")
                                .build())
                        .id("emission-1")
                        .build())
                .tickets(List.of())
                .orderDate(LocalDate.now())
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .build();
    }

    @Test
    @DisplayName("getAllTicketOrdersForLoggedUser — returns orders for user")
    void getAllTicketOrders_shouldReturnOrders() {
        when(ticketOrderRepository.findAllByUsername("test@example.com"))
                .thenReturn(Flux.just(sampleOrder));

        StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("test@example.com"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("order-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllTicketOrdersForLoggedUser — empty when no orders")
    void getAllTicketOrders_whenNoOrders_shouldReturnEmpty() {
        when(ticketOrderRepository.findAllByUsername("test@example.com"))
                .thenReturn(Flux.empty());

        StepVerifier.create(ticketOrderService.getAllTicketOrdersForLoggedUser("test@example.com"))
                .verifyComplete();
    }

    @Test
    @DisplayName("cancelOrder — emits Mono.error when orderId is null")
    void cancelOrder_whenOrderIdNull_shouldEmitError() {
        // Service returns Mono.error rather than throwing synchronously, so verify reactively.
        StepVerifier.create(ticketOrderService.cancelOrder("user", null))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketOrderServiceException.class)
                        .hasMessageContaining("null"))
                .verify();
    }

    @Test
    @DisplayName("cancelOrder — emits error when order id is unknown")
    void cancelOrder_whenOrderUnknown_shouldEmitError() {
        when(ticketOrderRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(ticketOrderService.cancelOrder("user", "missing"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketOrderServiceException.class)
                        .hasMessageContaining("missing"))
                .verify();
    }

    @Test
    @DisplayName("cancelOrder — owner can cancel: status changes to CANCELLED")
    void cancelOrder_whenOwnerCancels_shouldReturnCancelledOrder() {
        when(ticketOrderRepository.findById("order-1")).thenReturn(Mono.just(sampleOrder));

        StepVerifier.create(ticketOrderService.cancelOrder("test@example.com", "order-1"))
                .assertNext(dto -> assertThat(dto.getId()).isEqualTo("order-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("cancelOrder — throws when order belongs to different user")
    void cancelOrder_whenOrderNotOwned_shouldError() {
        var order = TicketOrder.builder()
                .id("order-1")
                .user(User.builder().username("other@example.com").build())
                .build();

        when(ticketOrderRepository.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(ticketOrderService.cancelOrder("attacker@example.com", "order-1"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(TicketOrderServiceException.class)
                        .hasMessageContaining("does not belong"))
                .verify();
    }
}