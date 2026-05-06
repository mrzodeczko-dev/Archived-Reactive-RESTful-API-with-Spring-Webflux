package com.rzodeczko.infrastructure.persistence.config.converter;

import com.rzodeczko.domain.vo.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class MoneyToStringConverter implements Converter<Money, String> {

    @Override
    public String convert(Money money) {
        return money.getValue().toString();
    }
}
