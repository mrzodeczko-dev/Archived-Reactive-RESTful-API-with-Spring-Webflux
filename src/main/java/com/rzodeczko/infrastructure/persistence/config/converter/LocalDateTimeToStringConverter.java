package com.rzodeczko.infrastructure.persistence.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WritingConverter
public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String convert(LocalDateTime localDateTime) {
        return dateTimeFormatter.format(localDateTime);
    }
}
