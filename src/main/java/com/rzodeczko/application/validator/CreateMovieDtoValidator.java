package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateMovieDto;
import com.rzodeczko.application.validator.generic.Validator;
import com.rzodeczko.domain.movie.enums.MovieGenre;
import org.apache.commons.validator.GenericValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CreateMovieDtoValidator implements Validator<CreateMovieDto, String> {

    @Override
    public Map<String, String> validate(CreateMovieDto item) {

        var errors = new HashMap<String, String>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }

        if (!isMovieGenreValid(item.genre())) {
            errors.put("genre %s".formatted(item.genre()), "is not valid");
        }

        if (!isMovieNameValid(item.name())) {
            errors.put("name %s".formatted(item.name()), "is not valid");
        }

        if (!isMovieDurationValid(item.duration())) {
            errors.put("duration %s".formatted(item.duration()), "is not valid");
        }

        if (!isPremiereDateValid(item.premiereDate())) {
            errors.put("premiere date %s".formatted(item.premiereDate()), "is not valid");
        }

        return errors;
    }

    private boolean isPremiereDateValid(String premiereDate) {

        var dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return nonNull(premiereDate) &&
                GenericValidator.isDate(premiereDate, "dd-MM-yyyy", true) &&
                LocalDate.from(dateTimeFormatter.parse(premiereDate)).isAfter(LocalDate.now());

    }

    private boolean isMovieDurationValid(Integer duration) {

        return nonNull(duration) && duration >= 1 && duration <= 5;
    }

    private boolean isMovieNameValid(String name) {
        return nonNull(name) && name.length() >= 2;
    }

    private boolean isMovieGenreValid(String genre) {
        return nonNull(genre) && MovieGenre.getAllMovieGenres().contains(genre);
    }
}
