package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.exception.CinemaServiceException;
import com.rzodeczko.application.port.out.CinemaCsvParserPort;
import com.rzodeczko.application.validator.CreateCinemaDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import lombok.RequiredArgsConstructor;
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
public class CsvCinemaParserAdapter implements CinemaCsvParserPort {

    private final CreateCinemaDtoValidator createCinemaDtoValidator;

    @Override
    public Mono<ParseResult<CreateCinemaDto>> parse(InputStream inputStream) {
        return Mono.fromCallable(() -> {
            var errors = new ArrayList<String>();
            var items = collectCinemasFromCsv(
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)),
                    errors);
            return ParseResult.of(items, errors);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<CreateCinemaDto> collectCinemasFromCsv(BufferedReader bufferedReader, List<String> errors) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvCinemaRow>(bufferedReader)
                    .withType(CsvCinemaRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var counterVal = counter.getAndIncrement();
                        var dto = row.toApplicationDto(errors, counterVal);
                        var validationErrors = createCinemaDtoValidator.validate(dto);
                        if (Validations.hasErrors(validationErrors)) {
                            errors.add("Cinema in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(validationErrors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof CinemaServiceException ce ? ce : new CinemaServiceException("The file extension .csv is required");
        }
    }
}
