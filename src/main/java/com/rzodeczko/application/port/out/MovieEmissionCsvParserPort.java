package com.rzodeczko.application.port.out;

import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface MovieEmissionCsvParserPort {
    Mono<ParseResult<CreateMovieEmissionDto>> parse(InputStream inputStream);
}
