package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateTicketPurchaseDto;
import com.rzodeczko.application.validator.generic.Validator;
import com.rzodeczko.application.validator.util.TicketBaseValidationUtils;

import java.util.Map;

public class CreateTicketPurchaseDtoValidator implements Validator<CreateTicketPurchaseDto, String> {

    @Override
    public Map<String, String> validate(CreateTicketPurchaseDto item) {

        return TicketBaseValidationUtils.validate(item);
    }

}
