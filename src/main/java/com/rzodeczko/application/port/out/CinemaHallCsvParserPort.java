package com.rzodeczko.application.port.out;

import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface CinemaHallCsvParserPort {
    Mono<ParseResult<CreateCinemaHallDto>> parse(InputStream inputStream);
}
