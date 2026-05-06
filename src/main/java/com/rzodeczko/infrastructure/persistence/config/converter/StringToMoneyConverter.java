package com.rzodeczko.infrastructure.persistence.config.converter;

import com.rzodeczko.domain.vo.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToMoneyConverter implements Converter<String, Money> {

    @Override
    public Money convert(String stringValue) {
        return Money.of(stringValue);
    }
}
