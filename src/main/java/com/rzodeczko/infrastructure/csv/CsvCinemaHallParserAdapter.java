package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.csv.ParseResult;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.exception.CinemaHallServiceException;
import com.rzodeczko.application.port.out.CinemaHallCsvParserPort;
import com.rzodeczko.application.validator.CreateCinemaHallDtoValidator;
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
public class CsvCinemaHallParserAdapter implements CinemaHallCsvParserPort {

    private final CreateCinemaHallDtoValidator createCinemaHallDtoValidator;

    @Override
    public Mono<ParseResult<CreateCinemaHallDto>> parse(InputStream inputStream) {
        return Mono.fromCallable(() -> {
            var errors = new ArrayList<String>();
            var items = collectCinemaHallsFromCsv(
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)),
                    errors);
            return ParseResult.of(items, errors);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<CreateCinemaHallDto> collectCinemaHallsFromCsv(BufferedReader bufferedReader, List<String> errors) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvCinemaHallRow>(bufferedReader)
                    .withType(CsvCinemaHallRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var dto = row.toApplicationDto();
                        var validationErrors = createCinemaHallDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();
                        if (Validations.hasErrors(validationErrors)) {
                            errors.add("Cinema hall in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(validationErrors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof CinemaHallServiceException ce ? ce : new CinemaHallServiceException("The file extension .csv is required");
        }
    }
}
