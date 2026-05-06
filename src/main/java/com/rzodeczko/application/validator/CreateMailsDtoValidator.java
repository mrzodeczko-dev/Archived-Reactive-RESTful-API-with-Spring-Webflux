package com.rzodeczko.application.validator;

import com.rzodeczko.application.dto.CreateMailsDto;
import com.rzodeczko.application.validator.generic.Validator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class CreateMailsDtoValidator implements Validator<CreateMailsDto, Object> {

    private final CreateMailDtoValidator singleEmailValidator = new CreateMailDtoValidator();

    @Override
    public Map<String, Object> validate(CreateMailsDto item) {

        var errors = new HashMap<String, Object>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }

        if (isNull(item.getMails()) || item.getMails().isEmpty()) {
            errors.put("mails", "are empty");
            return errors;
        }

        var counter = new AtomicInteger(1);

        item.getMails().forEach(mail ->
                errors.putAll(singleEmailValidator
                        .validate(mail)
                        .entrySet().stream()
                        .map(e -> Map.entry(counter.get(), Map.entry(e.getKey(), e.getValue())))
                        .collect(Collectors.collectingAndThen(Collectors.groupingBy(
                                e -> String.valueOf(e.getKey())
                        ), map -> {
                            counter.getAndIncrement();

                            return map.entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> e.getValue().stream()
                                                    .map(ee -> Map.entry(ee.getValue().getKey(), ee.getValue().getValue()))
                                                    .collect(Collectors.toList()),
                                            (oldVal, newVal) -> {
                                                oldVal.addAll(newVal);
                                                return oldVal;
                                            },
                                            LinkedHashMap::new
                                    ));
                        }))));

        return errors;
    }
}

