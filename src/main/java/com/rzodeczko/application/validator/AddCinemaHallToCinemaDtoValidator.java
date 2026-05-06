package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.AddCinemaHallToCinemaDto;
import com.rzodeczko.application.validator.generic.Validator;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class AddCinemaHallToCinemaDtoValidator implements Validator<AddCinemaHallToCinemaDto, String> {

    private static final Integer MIN_NUM_OF_POSITIONS = 50;

    @Override
    public Map<String, String> validate(AddCinemaHallToCinemaDto item) {

        var errors = new HashMap<String, String>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }

        if (isNull(item.getCinemaId())) {
            errors.put("cinema id", "is null");
        }

        // TODO: 02.02.2021
//        if (isNull(item.getPositions())) {
//            errors.put("positions", "is null");
//        } else if (item.getPositions() < MIN_NUM_OF_POSITIONS) {
//            errors.put("positions", "%s is less than min required: %s".formatted(item.getPositions(), MIN_NUM_OF_POSITIONS));
//        }

        return errors;
    }
}
