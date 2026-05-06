package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.validator.generic.Validator;
import org.apache.commons.validator.GenericValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CreateMovieEmissionDtoValidator implements Validator<CreateMovieEmissionDto, String> {

    @Override
    public Map<String, String> validate(CreateMovieEmissionDto item) {

        var errors = new HashMap<String, String>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }
        if (!isCinemaHallIdValid(item.getCinemaHallId())) {
            errors.put("cinemaHallId", "is null");
        }

        if (!isMovieIdValid(item.getMovieId())) {
            errors.put("movieId", "is null");
        }

        if (!isStartTimeValid(item.getStartTime())) {
            errors.put("start time: %s".formatted(item.getStartTime()), "is not valid. Valid format is: yyyy-MM-dd HH:mm");
        }

        if (isNull(item.getBaseTicketPrice())) {
            errors.put("base ticket price", "is required");
        } else if (!isBaseTicketPriceValid(item.getBaseTicketPrice())) {
            errors.put("base ticket price: %s".formatted(item.getBaseTicketPrice()), "should have valid format \\d+(\\.\\d{2})?");
        }
        return errors;
    }

    private boolean isBaseTicketPriceValid(String baseTicketPrice) {
        return baseTicketPrice.matches("\\d+(\\.\\d{2})?");
    }

    private boolean isStartTimeValid(String startTime) {
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return nonNull(startTime) &&
                GenericValidator.isDate(startTime, "yyyy-MM-dd HH:mm", true) &&
                LocalDate.from(dateTimeFormatter.parse(startTime)).compareTo(LocalDate.now()) > 0;
    }

    private boolean isMovieIdValid(String movieId) {
        return nonNull(movieId);
    }

    private boolean isCinemaHallIdValid(String cinemaHallId) {
        return nonNull(cinemaHallId);
    }
}
