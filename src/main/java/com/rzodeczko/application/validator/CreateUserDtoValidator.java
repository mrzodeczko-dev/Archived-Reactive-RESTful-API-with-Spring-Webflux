package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateUserDto;
import com.rzodeczko.application.validator.generic.Validator;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class CreateUserDtoValidator implements Validator<CreateUserDto, String> {

    private final String DATE_FORMAT = "dd-MM-yyyy";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private final EmailValidator emailValidator = EmailValidator.getInstance();

    @Override
    public Map<String, String> validate(CreateUserDto createUserDto) {
        var errors = new HashMap<String, String>();

        if (isNull(createUserDto)) {
            errors.put("dto object", "is null");
            return errors;
        }

        errors.putAll(validateBirthDate(createUserDto.getBirthDate()));
        errors.putAll(validateUsername(createUserDto.getUsername()));
        errors.putAll(validatePassword(createUserDto.getPassword(), createUserDto.getPasswordConfirmation()));
        errors.putAll(validateEmail(createUserDto.getEmail()));

        return errors;
    }

    private Map<String, String> validateBirthDate(String birthDate) {

        var errors = new HashMap<String, String>();

        if (isNull(birthDate)) {
            errors.put("Birth date", "is null");
        } else if (!GenericValidator.isDate(birthDate, DATE_FORMAT, true)) {
            errors.put("Birth date %s:".formatted(birthDate), "Birth date format should be: %s".formatted(DATE_FORMAT));
        } else if (LocalDate.now().minusYears(18).compareTo(LocalDate.from(dateTimeFormatter.parse(birthDate))) < 0) {
            errors.put("Birt date", "User has to be an adult");
        }

        return errors;
    }

    private Map<String, String> validateUsername(String username) {

        var errors = new HashMap<String, String>();

        if (isNull(username)) {
            errors.put("Username", "is null");
        } else if (username.length() < 5) {
            errors.put("Username", "Minimum length of user is 5 characters");
        }

        return errors;
    }

    private Map<String, String> validatePassword(String password, String passwordConfirmation) {
        var errors = new HashMap<String, String>();

        if (isNull(password) || isNull(passwordConfirmation)) {
            errors.put("Password", "password and confirmation password must be set");
        } else {
            if (password.length() < 5) {
                errors.put("Password", "Minimum password length is 5 characters");
            }
            if (!password.equals(passwordConfirmation)) {
                errors.put("Password confirmation", "Password and confirmation password does not match");
            }

        }
        return errors;
    }

    private Map<String, String> validateEmail(String email) {

        var errors = new HashMap<String, String>();

        if (isNull(email)) {
            errors.put("email", "is null");
            return errors;
        }

        if (!emailValidator.isValid(email)) {
            errors.put("email", "is not valid");
        }

        return errors;
    }

}

