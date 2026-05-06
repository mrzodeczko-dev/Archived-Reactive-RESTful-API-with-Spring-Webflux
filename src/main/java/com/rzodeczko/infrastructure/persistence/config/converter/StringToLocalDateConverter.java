package com.rzodeczko.infrastructure.persistence.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;

@ReadingConverter
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String stringVal) {
        return LocalDate.parse(stringVal);
    }
}
