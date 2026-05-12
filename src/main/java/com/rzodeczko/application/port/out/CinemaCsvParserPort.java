package com.rzodeczko.application.port.out;

import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateCinemaDto;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface CinemaCsvParserPort {
    Mono<ParseResult<CreateCinemaDto>> parse(InputStream inputStream);
}
