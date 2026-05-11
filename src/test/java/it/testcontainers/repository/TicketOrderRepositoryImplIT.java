package it.testcontainers.repository;

import com.rzodeczko.application.port.out.TicketOrderPort;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import com.rzodeczko.domain.user.User;
import com.rzodeczko.infrastructure.persistence.document.TicketOrderDocument;
import com.rzodeczko.infrastructure.persistence.repository.impl.TickerOrderRepositoryImpl;
import it.testcontainers.AbstractMongoIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(TickerOrderRepositoryImpl.class)   // intentional: matches existing class name
class TicketOrderRepositoryImplIT extends AbstractMongoIT {

    @Autowired
    private TicketOrderPort ticketOrderPort;
    @Autowired
    private ReactiveMongoTemplate template;

    private User jan;
    private User anna;

    @BeforeEach
    void wipeAndSeed() {
        template.dropCollection(TicketOrderDocument.class).block();

        jan = User.builder().username("jan").password("h1").email("jan@example.com").build();
        anna = User.builder().username("anna").password("h2").email("anna@example.com").build();

        TicketOrder janOrder1 = TicketOrder.builder()
                .user(jan).orderDate(LocalDate.of(2026, 5, 1))
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .ticketGroupType(TicketGroupType.NORMAL)
                .tickets(Collections.emptyList()).build();
        TicketOrder janOrder2 = TicketOrder.builder()
                .user(jan).orderDate(LocalDate.of(2026, 5, 2))
                .ticketOrderStatus(TicketOrderStatus.DONE)
                .ticketGroupType(TicketGroupType.NORMAL)
                .tickets(Collections.emptyList()).build();
        TicketOrder annaOrder = TicketOrder.builder()
                .user(anna).orderDate(LocalDate.of(2026, 5, 3))
                .ticketOrderStatus(TicketOrderStatus.ORDERED)
                .ticketGroupType(TicketGroupType.NORMAL)
                .tickets(Collections.emptyList()).build();

        ticketOrderPort.addOrUpdateMany(List.of(janOrder1, janOrder2, annaOrder)).blockLast();
    }

    @Test
    @DisplayName("findAllByUsername — derived query through nested user.username field")
    void shouldFindAllForUser() {
        StepVerifier.create(ticketOrderPort.findAllByUsername("jan").collectList())
                .assertNext(l -> {
                    assertThat(l).hasSize(2);
                    assertThat(l).extracting(o -> o.getUser().getUsername())
                            .containsOnly("jan");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllByUsername of unknown user → empty Flux")
    void shouldReturnEmptyForUnknownUser() {
        StepVerifier.create(ticketOrderPort.findAllByUsername("ghost").collectList())
                .assertNext(l -> assertThat(l).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("Order status (ORDERED / DONE) round-trips through Mongo")
    void shouldPreserveOrderStatus() {
        StepVerifier.create(ticketOrderPort.findAllByUsername("jan").collectList())
                .assertNext(l -> assertThat(l).extracting(TicketOrder::getTicketOrderStatus)
                        .containsExactlyInAnyOrder(TicketOrderStatus.ORDERED, TicketOrderStatus.DONE))
                .verifyComplete();
    }
}