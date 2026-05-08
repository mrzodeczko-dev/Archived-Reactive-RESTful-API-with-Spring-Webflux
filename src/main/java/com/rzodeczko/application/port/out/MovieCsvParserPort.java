package com.rzodeczko.application.port.out;

import com.rzodeczko.application.dto.CreateMovieDto;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.List;

public interface MovieCsvParserPort {
    Flux<CreateMovieDto> parse(InputStream inputStream, List<String> errorsList);
}