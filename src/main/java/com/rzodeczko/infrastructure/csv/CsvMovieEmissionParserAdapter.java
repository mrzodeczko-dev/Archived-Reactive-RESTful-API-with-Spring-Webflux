package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.exception.MovieEmissionServiceException;
import com.rzodeczko.application.port.out.MovieEmissionCsvParserPort;
import com.rzodeczko.application.validator.CreateMovieEmissionDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
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
public class CsvMovieEmissionParserAdapter implements MovieEmissionCsvParserPort {

    private final CreateMovieEmissionDtoValidator createMovieEmissionDtoValidator;

    public CsvMovieEmissionParserAdapter(CreateMovieEmissionDtoValidator createMovieEmissionDtoValidator) {
        this.createMovieEmissionDtoValidator = createMovieEmissionDtoValidator;
    }

    @Override
    public Mono<ParseResult<CreateMovieEmissionDto>> parse(InputStream inputStream) {
        return Mono.fromCallable(() -> {
            var errors = new ArrayList<String>();
            var items = collectMovieEmissionsFromCsv(
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)),
                    errors);
            return ParseResult.of(items, errors);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<CreateMovieEmissionDto> collectMovieEmissionsFromCsv(BufferedReader bufferedReader, List<String> errors) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvMovieEmissionRow>(bufferedReader)
                    .withType(CsvMovieEmissionRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var dto = row.toApplicationDto();
                        var validationErrors = createMovieEmissionDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();
                        if (Validations.hasErrors(validationErrors)) {
                            errors.add("Movie emission in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(validationErrors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof MovieEmissionServiceException mee ? mee : new MovieEmissionServiceException("The file extension .csv is required");
        }
    }
}
