package com.rzodeczko.infrastructure.persistence.document;

import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ticket_orders")
public class TicketOrderDocument {

    @Id
    private String id;
    private UserDocument user;
    private LocalDate orderDate;
    private TicketOrderStatus ticketOrderStatus;
    private MovieEmissionDocument movieEmission;
    private List<TicketDocument> tickets;

    @Field("ticket_order_type")
    private TicketGroupType ticketGroupType;

}
