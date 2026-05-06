package com.rzodeczko.infrastructure.persistence.document;

import com.rzodeczko.domain.ticket.enums.IndividualTicketType;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.vo.Discount;
import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tickets")
public class TicketDocument {

    @Id
    private String id;
    private TicketStatus ticketStatus;
    private IndividualTicketType type;
    private Position position;
    private Discount discount;
    private Money price;
}
