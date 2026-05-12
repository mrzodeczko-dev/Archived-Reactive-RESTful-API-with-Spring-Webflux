package com.rzodeczko.application.port.out;

import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateCityDto;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface CityCsvParserPort {
    Mono<ParseResult<CreateCityDto>> parse(InputStream inputStream);
}
