package com.rzodeczko.application.dto;

import com.rzodeczko.application.dto.contract.TicketDtoMarker;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateTicketOrderDto implements TicketDtoMarker {

    private String movieEmissionId;
    private List<TicketDetailsDto> ticketsDetails;
    private TicketGroupType ticketGroupType;

}

