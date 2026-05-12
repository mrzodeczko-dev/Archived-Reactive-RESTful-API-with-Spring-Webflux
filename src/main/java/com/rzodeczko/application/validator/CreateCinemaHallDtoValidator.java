package com.rzodeczko.application.validator;


import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.validator.generic.Validator;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CreateCinemaHallDtoValidator implements Validator<CreateCinemaHallDto, String> {

    private static final Integer MIN_NUMBER_OF_ROWS_AND_COL = 5;

    @Override
    public Map<String, String> validate(CreateCinemaHallDto item) {

        var errors = new HashMap<String, String>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }

        if (!isColNoValid(item.colNo())) {
            errors.put("colNo", "[%d] is not valid. Min required is: %s".formatted(item.colNo(), MIN_NUMBER_OF_ROWS_AND_COL));
        }

        if (!isRowNoValid(item.rowNo())) {
            errors.put("rowNo", "[%d] is not valid. Min required is: %s".formatted(item.rowNo(), MIN_NUMBER_OF_ROWS_AND_COL));
        }

        return errors;
    }

    private boolean isNumberValid(Integer number) {
        return nonNull(number) && number >= MIN_NUMBER_OF_ROWS_AND_COL;
    }

    private boolean isColNoValid(Integer colNo) {
        return isNumberValid(colNo);
    }

    private boolean isRowNoValid(Integer rowNo) {
        return isNumberValid(rowNo);
    }
}
