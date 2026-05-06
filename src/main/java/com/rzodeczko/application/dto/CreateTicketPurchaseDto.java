package com.rzodeczko.application.dto;

import com.rzodeczko.application.dto.contract.TicketDtoMarker;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateTicketPurchaseDto implements TicketDtoMarker {

    private String movieEmissionId;
    private List<TicketDetailsDto> ticketsDetails;
    private TicketGroupType ticketGroupType;

}
