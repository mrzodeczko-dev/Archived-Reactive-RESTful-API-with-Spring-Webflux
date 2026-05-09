package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.SendEmailToSelfDto;
import com.rzodeczko.application.validator.generic.Validator;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class SendEmailToSelfDtoValidator implements Validator<SendEmailToSelfDto, String> {

    @Override
    public Map<String, String> validate(SendEmailToSelfDto item) {
        var errors = new HashMap<String, String>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }
        if (StringUtils.isBlank(item.title())) {
            errors.put("title", "is required");
        }
        if (StringUtils.isBlank(item.htmlContent())) {
            errors.put("htmlContent", "is required");
        }
        return errors;
    }
}