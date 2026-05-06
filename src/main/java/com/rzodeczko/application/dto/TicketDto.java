package com.rzodeczko.application.dto;

import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TicketDto {

    private String id;

    private TicketStatus ticketStatus;
    private IndividualTicketType type;
    private Position position;
    private Discount discount;
    private Money price;
}
