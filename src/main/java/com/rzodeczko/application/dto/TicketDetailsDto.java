package com.rzodeczko.application.dto;

import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TicketDetailsDto {

    private IndividualTicketType individualTicketType;
    private Position position;

}
