package com.rzodeczko.application.dto.contract;

import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Position;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public interface TicketDtoMarker {

    String getMovieEmissionId();

    List<TicketDetailsDto> getTicketsDetails();

    TicketGroupType getTicketGroupType();

    default boolean areAllPositionsAvailable(List<Position> freePositions) {
        return getTicketsDetails().stream()
                .map(TicketDetailsDto::getPosition)
                .allMatch(freePositions::contains);
    }

    @JsonIgnore
    default Discount getBaseDiscount() {
        return getTicketGroupType().getDiscount();
    }

}
