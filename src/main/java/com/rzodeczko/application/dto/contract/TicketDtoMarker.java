package com.rzodeczko.application.dto.contract;

import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Position;

import java.util.List;

public interface TicketDtoMarker {

    String movieEmissionId();

    List<TicketDetailsDto> ticketsDetails();

    TicketGroupType ticketGroupType();

    default boolean areAllPositionsAvailable(List<Position> freePositions) {
        return ticketsDetails().stream()
                .map(TicketDetailsDto::position)
                .allMatch(freePositions::contains);
    }

    default Discount getBaseDiscount() {
        return ticketGroupType().getDiscount();
    }

}
