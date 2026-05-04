package com.app.application.validator;

import com.app.application.dto.CreateCinemaDto;
import com.app.application.dto.CreateCinemaHallDto;
import com.app.application.validator.generic.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Component
public class CreateCinemaDtoValidator implements Validator<CreateCinemaDto, String> {

    @Override
    public Map<String, String> validate(CreateCinemaDto item) {

        var errors = new LinkedHashMap<String, String>();

        if (isNull(item)) {
            errors.put("dt object", "is null");
            return errors;
        }

        if (isBlank(item.getCity())) {
            errors.put("City name", "is blank");
        }

        if (isNull(item.getCinemaHallsCapacity())) {
            errors.put("Cinema halls capacity", "are required");
        } else {
            errors.putAll(validateCinemaHalls(item.getCinemaHallsCapacity()));
        }

        return errors;
    }

    private Map<String, String> validateCinemaHalls(List<CreateCinemaHallDto> cinemaHalls) {
        var counter = new AtomicInteger(1);
        var errors = new HashMap<String, String>();

        cinemaHalls
                .forEach(createCinemaHallDto -> {
                    var counterVal = counter.getAndIncrement();
                    var errorsSingle = validateSingleHallCapacity(createCinemaHallDto, counterVal);
                    if (!errorsSingle.isEmpty()) {
                        errors.put("cinemaHall no. %d".formatted(counterVal), errorsSingle.get("cinemaHall no. %d".formatted(counterVal)));
                    }

                });

        return errors;

    }

    private Map<String, String> validateSingleHallCapacity(CreateCinemaHallDto createCinemaHallDto, Integer counter) {

        var errors = new LinkedHashMap<String, String>();

        if (isNull(createCinemaHallDto)) {
            errors.put("cinemaHall no. %d".formatted(counter), "is null");
        } else if (!isSingleHallCapacityValid(createCinemaHallDto)) {
            errors.put(
                    "cinemaHall no. %d".formatted(counter),
                    "cinema hall row and col numbers must be positive integer, actual values are: row: %d, col: %d".formatted(createCinemaHallDto.getRowNo(), createCinemaHallDto.getColNo()));
        }

        return errors;
    }

    private boolean isSingleHallCapacityValid(CreateCinemaHallDto createCinemaHallDto) {
        return createCinemaHallDto.getColNo() > 0 && createCinemaHallDto.getRowNo() > 0;
    }


}
