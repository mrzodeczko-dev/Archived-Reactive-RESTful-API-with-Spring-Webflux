package com.rzodeczko.application.port.out;

import com.rzodeczko.domain.ticket_order.TicketOrder;
import reactor.core.publisher.Flux;

public interface TicketOrderPort extends CrudPort<TicketOrder, String> {

    Flux<TicketOrder> findAllByUsername(String username);
}
