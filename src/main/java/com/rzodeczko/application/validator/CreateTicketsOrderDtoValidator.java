package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateTicketOrderDto;
import com.rzodeczko.application.validator.generic.Validator;
import com.rzodeczko.application.validator.util.TicketBaseValidationUtils;
import com.rzodeczko.domain.ticket_order.enums.TicketGroupType;

import java.util.Map;

import static java.util.Objects.nonNull;

public class CreateTicketsOrderDtoValidator implements Validator<CreateTicketOrderDto, String> {

    @Override
    public Map<String, String> validate(CreateTicketOrderDto item) {
        return validateTicketOrder(TicketBaseValidationUtils.validate(item), item);
    }

    private boolean isTicketOrderTypeValid(TicketGroupType ticketGroupType) {
        return nonNull(ticketGroupType);
    }

    private Map<String, String> validateTicketOrder(Map<String, String> currentErrors, CreateTicketOrderDto item) {

        if (nonNull(item) && !isTicketOrderTypeValid(item.getTicketGroupType())) {
            currentErrors.put("ticketOrderType {%s}".formatted(item.getTicketGroupType()), "is not valid");
        }

        return currentErrors;
    }

}
