package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.dto.CreateMovieDto;
import com.rzodeczko.application.exception.MovieServiceException;
import com.rzodeczko.application.port.out.MovieCsvParserPort;
import com.rzodeczko.application.validator.CreateMovieDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CsvMovieParserAdapter implements MovieCsvParserPort {

    private static final Logger log = LogManager.getLogger(CsvMovieParserAdapter.class);

    private final CreateMovieDtoValidator createMovieDtoValidator;

    public CsvMovieParserAdapter(CreateMovieDtoValidator createMovieDtoValidator) {
        this.createMovieDtoValidator = createMovieDtoValidator;
    }

    public Flux<CreateMovieDto> parse(InputStream inputStream, List<String> errorsList) {
        return Mono.fromCallable(() -> collectMoviesFromCsv(new BufferedReader(new InputStreamReader(inputStream)), errorsList))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(Function.identity());
    }

    private List<CreateMovieDto> collectMoviesFromCsv(BufferedReader bufferedReader, List<String> errorsList) {
        try {
            var counter = new AtomicInteger(1);

            return new CsvToBeanBuilder<CsvMovieRow>(bufferedReader)
                    .withType(CsvMovieRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .peek(row -> {
                        var dto = row.toApplicationDto();
                        var errors = createMovieDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();
                        if (Validations.hasErrors(errors)) {
                            errorsList.add("Movie in row no. %s is not valid. %s".formatted(counterVal, Validations.createErrorMessage(errors)));
                        }
                    })
                    .map(CsvMovieRow::toApplicationDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw e instanceof MovieServiceException me ? me : new MovieServiceException("The file extension .csv is required");
        }
    }
}