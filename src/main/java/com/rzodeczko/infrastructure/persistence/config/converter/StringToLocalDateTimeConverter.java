package com.rzodeczko.infrastructure.persistence.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ReadingConverter
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public LocalDateTime convert(String stringValue) {
        return LocalDateTime.from(dateTimeFormatter.parse(stringValue));
    }
}
