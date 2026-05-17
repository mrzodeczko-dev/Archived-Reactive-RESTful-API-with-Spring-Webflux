package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateMovieDto;
import com.rzodeczko.application.exception.MovieServiceException;
import com.rzodeczko.application.port.out.MovieCsvParserPort;
import com.rzodeczko.application.validator.CreateMovieDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class CsvMovieParserAdapter implements MovieCsvParserPort {

    private static final Logger log = LogManager.getLogger(CsvMovieParserAdapter.class);

    private final CreateMovieDtoValidator createMovieDtoValidator;

    @Override
    public Mono<ParseResult<CreateMovieDto>> parse(InputStream inputStream) {
        return Mono.fromCallable(() -> {
            var errors = new ArrayList<String>();
            var items = collectMoviesFromCsv(
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)),
                    errors);
            return ParseResult.of(items, errors);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<CreateMovieDto> collectMoviesFromCsv(BufferedReader bufferedReader, List<String> errors) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvMovieRow>(bufferedReader)
                    .withType(CsvMovieRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var dto = row.toApplicationDto();
                        var validationErrors = createMovieDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();
                        if (Validations.hasErrors(validationErrors)) {
                            errors.add("Movie in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(validationErrors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof MovieServiceException me ? me : new MovieServiceException("The file extension .csv is required");
        }
    }
}
