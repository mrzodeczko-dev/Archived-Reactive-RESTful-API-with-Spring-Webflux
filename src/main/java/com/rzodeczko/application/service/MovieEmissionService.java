package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.application.exception.MovieEmissionServiceException;
import com.rzodeczko.application.mapper.MovieEmissionMapper;
import com.rzodeczko.application.port.out.*;
import com.rzodeczko.application.service.util.DateTimeGapFinder;
import com.rzodeczko.application.validator.CreateMovieEmissionDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.domain.vo.Money;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Interval;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MovieEmissionService {

    private static final Integer MINIMAL_BREAK_BETWEEN_MOVIE_EMISSIONS_IN_MIN = 10;

    private final MovieEmissionPort movieEmissionPort;
    private final CinemaHallPort cinemaHallPort;
    private final MoviePort moviePort;
    private final MovieEmissionCsvParserPort movieEmissionCsvParserPort;
    private final TransactionPort transactionPort;
    private final CreateMovieEmissionDtoValidator createMovieEmissionDtoValidator;

    public MovieEmissionService(MovieEmissionPort movieEmissionPort, CinemaHallPort cinemaHallPort,
                                MoviePort moviePort, MovieEmissionCsvParserPort movieEmissionCsvParserPort,
                                TransactionPort transactionPort,
                                CreateMovieEmissionDtoValidator createMovieEmissionDtoValidator) {
        this.movieEmissionPort = movieEmissionPort;
        this.cinemaHallPort = cinemaHallPort;
        this.moviePort = moviePort;
        this.movieEmissionCsvParserPort = movieEmissionCsvParserPort;
        this.transactionPort = transactionPort;
        this.createMovieEmissionDtoValidator = createMovieEmissionDtoValidator;
    }

    public Mono<MovieEmissionDto> createMovieEmission(CreateMovieEmissionDto createMovieEmission) {

        var errors = createMovieEmissionDtoValidator.validate(createMovieEmission);
        if (Validations.hasErrors(errors)) {
            return Mono.error(new MovieEmissionServiceException(Validations.createErrorMessage(errors)));
        }

        Mono<MovieEmission> result = saveMovieEmission(createMovieEmission);

        return transactionPort.inTransaction(result).map(MovieEmissionMapper::toDto);
    }

    private Mono<MovieEmission> saveMovieEmission(CreateMovieEmissionDto createMovieEmission) {
        return moviePort.findById(createMovieEmission.movieId())
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie with id: [%s]".formatted(createMovieEmission.movieId()))))
                .flatMap(movie -> {
                    if (movie.premiereDate().isAfter(toLocalDateTime(createMovieEmission.startTime()).toLocalDate())) {
                        return Mono.error(new MovieEmissionServiceException(
                                "Movie with id: %s cannot be displayed in %s - before premiere date: %s"
                                        .formatted(createMovieEmission.movieId(),
                                                createMovieEmission.startTime(),
                                                movie.premiereDate())));
                    }
                    return cinemaHallPort.findById(createMovieEmission.cinemaHallId())
                            .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                                    "No cinema hall with id: [%s]".formatted(createMovieEmission.cinemaHallId()))))
                            .map(x -> Pair.of(x, movie));
                })
                .flatMap(pair -> {
                    if (!pair.getLeft().movieEmissions().isEmpty()
                            && !isFreeSpaceForMovieEmissionInCinemaHall(
                            pair.getLeft(), pair.getRight(), createMovieEmission.startTime())) {
                        return Mono.error(new MovieEmissionServiceException(
                                "No time space for this movieEmission in this cinema hall!"));
                    }
                    return movieEmissionPort.addOrUpdate(
                                    MovieEmission.builder()
                                            .movie(pair.getRight())
                                            .startDateTime(toLocalDateTime(createMovieEmission.startTime()))
                                            .cinemaHallId(pair.getLeft().id())
                                            .baseTicketPrice(Money.of(createMovieEmission.baseTicketPrice()))
                                            .isPositionFree(pair.getLeft().positions().stream()
                                                    .collect(Collectors.toMap(
                                                            Function.identity(),
                                                            position -> true,
                                                            (old, _) -> old,
                                                            LinkedHashMap::new)))
                                            .build())
                            .map(movieEmission -> Pair.of(pair.getLeft(), movieEmission));
                })
                .flatMap(pair -> cinemaHallPort.addOrUpdate(pair.getLeft().addMovieEmission(pair.getRight()))
                        .then(Mono.just(pair.getRight())));
    }

    private boolean isFreeSpaceForMovieEmissionInCinemaHall(
            CinemaHall cinemaHall, Movie movie, String startDateTimeOfMovieEmission) {

        var startDateTime = toLocalDateTime(startDateTimeOfMovieEmission);
        var movieEmissionTimesInDay = getMovieEmissionTimesInDay(startDateTime.toLocalDate(), cinemaHall);

        if (movieEmissionTimesInDay.isEmpty()) {
            return true;
        }

        int size = movieEmissionTimesInDay.size();

        var bookedTimeSpace = IntStream.range(0, size)
                .mapToObj(i -> {
                    Interval interval = movieEmissionTimesInDay.get(i);
                    if (i == 0 || i == size - 1) {
                        return new Interval(
                                interval.getStart().minusMinutes(MINIMAL_BREAK_BETWEEN_MOVIE_EMISSIONS_IN_MIN),
                                interval.getEnd().plusMinutes(MINIMAL_BREAK_BETWEEN_MOVIE_EMISSIONS_IN_MIN));
                    }
                    return interval;
                })
                .collect(Collectors.toList());

        var endDateTime = startDateTime.plusMinutes(movie.duration());
        var movieInterval = new Interval(
                startDateTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                endDateTime.toEpochSecond(ZoneOffset.UTC) * 1000);

        return DateTimeGapFinder
                .findGaps(bookedTimeSpace, dayToInterval(startDateTime.toLocalDate()))
                .stream()
                .anyMatch(interval -> interval.contains(movieInterval));
    }

    public Flux<MovieEmissionDto> getAllMovieEmissions() {
        return movieEmissionPort.findAll().map(MovieEmissionMapper::toDto);
    }

    public Flux<MovieEmissionDto> getAllMovieEmissionsByMovieId(String movieId) {
        return movieEmissionPort.findMovieEmissionsByMovieId(movieId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie hall with id: %s".formatted(movieId))))
                .map(MovieEmissionMapper::toDto);
    }

    public Flux<MovieEmissionDto> getAllMovieEmissionsByCinemaHallId(String cinemaHallId) {
        return movieEmissionPort.findMovieEmissionsByCinemaHallId(cinemaHallId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No cinema hall with id: %s".formatted(cinemaHallId))))
                .map(MovieEmissionMapper::toDto);
    }

    public Flux<MovieEmissionDto> getMovieEmissionsByCinemaAndMovie(String cinemaId, String movieId,
                                                                    LocalDateTime fromDate, LocalDateTime toDate) {

        return cinemaHallPort.getAllForCinemaById(cinemaId)
                .map(CinemaHall::id)
                .collectList()
                .flatMapMany(chIds -> movieEmissionPort.findMovieEmissionsByCinemaHallIdInAndStartDateTimeBetweenAndMovieId(
                        chIds,
                        Optional.ofNullable(fromDate).orElse(LocalDateTime.now()),
                        Optional.ofNullable(toDate).orElse(LocalDateTime.now().plusDays(7)),
                        movieId))
                .map(MovieEmissionMapper::toDto);
    }

    public Mono<MovieEmissionDto> deleteMovieEmission(String movieEmissionId) {
        Mono<MovieEmission> result = movieEmissionPort.deleteById(movieEmissionId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie emission with id: %s".formatted(movieEmissionId))))
                .flatMap(movieEmission -> cinemaHallPort.getByMovieEmissionId(movieEmissionId)
                        .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                                "No cinema hall connected to movie emission with id %s".formatted(movieEmissionId))))
                        .flatMap(cinemaHall -> cinemaHallPort
                                .addOrUpdate(cinemaHall.removeMovieEmissionById(movieEmission.id()))
                                .then(Mono.just(movieEmission))));
        return transactionPort.inTransaction(result).map(MovieEmissionMapper::toDto);
    }

    public Flux<MovieEmissionDto> uploadCSVFile(InputStream inputStream) {
        return movieEmissionCsvParserPort.parse(inputStream)
                .flatMapMany(result -> {
                    if (result.hasErrors()) {
                        return Flux.error(new MovieEmissionServiceException("Errors are: %s".formatted(result.errors())));
                    }
                    return transactionPort.inTransactionMany(
                            Flux.fromIterable(result.items()).concatMap(this::saveMovieEmission)
                    ).map(MovieEmissionMapper::toDto);
                });
    }

    private LocalDateTime toLocalDateTime(String stringValue) {
        return LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").parse(stringValue));
    }

    private Interval dayToInterval(LocalDate date) {
        return new Interval(
                date.atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * 1000,
                date.plusDays(1).atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    private List<Interval> getMovieEmissionTimesInDay(LocalDate date, CinemaHall cinemaHall) {
        return cinemaHall.movieEmissions()
                .stream()
                .filter(movieEmission -> movieEmission.startDateTime().toLocalDate().isEqual(date))
                .sorted(Comparator.comparing(MovieEmission::startDateTime))
                .map(movieEmission -> new Interval(
                        movieEmission.startDateTime().toEpochSecond(ZoneOffset.UTC) * 1000,
                        movieEmission.startDateTime()
                                .plusMinutes(movieEmission.movie().duration())
                                .toEpochSecond(ZoneOffset.UTC) * 1000))
                .collect(Collectors.toList());
    }
}