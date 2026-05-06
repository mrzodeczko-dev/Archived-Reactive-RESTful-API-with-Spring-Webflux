package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.dto.MovieEmissionDto;
import com.rzodeczko.application.exception.MovieEmissionServiceException;
import com.rzodeczko.application.mapper.MovieEmissionMapper;
import com.rzodeczko.application.port.out.CinemaHallPort;
import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.application.port.out.TransactionPort;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MovieEmissionService {

    private static final Integer MINIMAL_BREAK_BETWEEN_MOVIE_EMISSIONS_IN_MIN = 10;

    private final MovieEmissionPort movieEmissionPort;
    private final CinemaHallPort cinemaHallPort;
    private final MoviePort moviePort;
    private final TransactionPort transactionPort;

    public MovieEmissionService(MovieEmissionPort movieEmissionPort, CinemaHallPort cinemaHallPort,
                                MoviePort moviePort, TransactionPort transactionPort) {
        this.movieEmissionPort = movieEmissionPort;
        this.cinemaHallPort = cinemaHallPort;
        this.moviePort = moviePort;
        this.transactionPort = transactionPort;
    }

    public Mono<MovieEmissionDto> createMovieEmission(CreateMovieEmissionDto createMovieEmission) {

        var errors = new CreateMovieEmissionDtoValidator().validate(createMovieEmission);
        if (Validations.hasErrors(errors)) {
            return Mono.error(new MovieEmissionServiceException(Validations.createErrorMessage(errors)));
        }

        Mono<MovieEmission> result = moviePort.findById(createMovieEmission.getMovieId())
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie with id: [%s]".formatted(createMovieEmission.getMovieId()))))
                .flatMap(movie -> {
                    if (movie.getPremiereDate().compareTo(
                            toLocalDateTime(createMovieEmission.getStartTime()).toLocalDate()) > 0) {
                        return Mono.error(new MovieEmissionServiceException(
                                "Movie with id: %s cannot be displayed in %s - before premiere date: %s"
                                        .formatted(createMovieEmission.getMovieId(),
                                                createMovieEmission.getStartTime(),
                                                movie.getPremiereDate())));
                    }
                    return cinemaHallPort.findById(createMovieEmission.getCinemaHallId())
                            .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                                    "No cinema hall with id: [%s]".formatted(createMovieEmission.getCinemaHallId()))))
                            .map(x -> Pair.of(x, movie));
                })
                .flatMap(pair -> {
                    if (!pair.getLeft().getMovieEmissions().isEmpty()
                            && !isFreeSpaceForMovieEmissionInCinemaHall(
                            pair.getLeft(), pair.getRight(), createMovieEmission.getStartTime())) {
                        return Mono.error(new MovieEmissionServiceException(
                                "No time space for this movieEmission in this cinema hall!"));
                    }
                    return movieEmissionPort.addOrUpdate(
                                    MovieEmission.builder()
                                            .movie(pair.getRight())
                                            .startDateTime(toLocalDateTime(createMovieEmission.getStartTime()))
                                            .cinemaHallId(pair.getLeft().getId())
                                            .baseTicketPrice(Money.of(createMovieEmission.getBaseTicketPrice()))
                                            .isPositionFree(pair.getLeft().getPositions().stream()
                                                    .collect(Collectors.toMap(
                                                            Function.identity(),
                                                            position -> true,
                                                            (old, newVal) -> old,
                                                            LinkedHashMap::new)))
                                            .build())
                            .map(movieEmission -> Pair.of(pair.getLeft(), movieEmission));
                })
                .flatMap(pair -> {
                    pair.getLeft().getMovieEmissions().add(pair.getRight());
                    return cinemaHallPort.addOrUpdate(pair.getLeft()).then(Mono.just(pair.getRight()));
                });

        return transactionPort.inTransaction(result).map(MovieEmissionMapper::toDto);
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

        var endDateTime = startDateTime.plusHours(movie.getDuration());
        var movieInterval = new Interval(
                startDateTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                endDateTime.toEpochSecond(ZoneOffset.UTC) * 1000);

        return DateTimeGapFinder
                .findGaps(bookedTimeSpace, dayToInterval(startDateTime.toLocalDate()))
                .stream()
                .anyMatch(interval -> interval.contains(movieInterval));
    }

    private Interval dayToInterval(LocalDate date) {
        return new Interval(
                date.atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * 1000,
                date.plusDays(1).atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    private List<Interval> getMovieEmissionTimesInDay(LocalDate date, CinemaHall cinemaHall) {
        return cinemaHall.getMovieEmissions()
                .stream()
                .filter(movieEmission -> movieEmission.getStartDateTime().toLocalDate().compareTo(date) == 0)
                .sorted(Comparator.comparing(MovieEmission::getStartDateTime))
                .map(movieEmission -> new Interval(
                        movieEmission.getStartDateTime().toEpochSecond(ZoneOffset.UTC) * 1000,
                        movieEmission.getStartDateTime()
                                .plusHours(movieEmission.getMovie().getDuration())
                                .toEpochSecond(ZoneOffset.UTC) * 1000))
                .collect(Collectors.toList());
    }

    private LocalDateTime toLocalDateTime(String stringValue) {
        return LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").parse(stringValue));
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

    public Mono<MovieEmissionDto> deleteMovieEmission(String movieEmissionId) {
        Mono<MovieEmission> result = movieEmissionPort.deleteById(movieEmissionId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie emission with id: %s".formatted(movieEmissionId))))
                .flatMap(movieEmission -> cinemaHallPort.getByMovieEmissionId(movieEmissionId)
                        .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                                "No cinema hall connected to movie emission with id %s".formatted(movieEmissionId))))
                        .flatMap(cinemaHall -> cinemaHallPort
                                .addOrUpdate(cinemaHall.removeMovieEmissionById(movieEmission.getId()))
                                .then(Mono.just(movieEmission))));
        return transactionPort.inTransaction(result).map(MovieEmissionMapper::toDto);
    }
}
