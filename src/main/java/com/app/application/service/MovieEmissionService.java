package com.app.application.service;

import com.app.application.dto.CreateMovieEmissionDto;
import com.app.application.dto.MovieEmissionDto;
import com.app.application.exception.MovieEmissionServiceException;
import com.app.application.service.util.DateTimeGapFinder;
import com.app.application.validator.CreateMovieEmissionDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.movie.Movie;
import com.app.domain.movie.MovieRepository;
import com.app.domain.movie_emission.MovieEmission;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
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

@Service
@RequiredArgsConstructor
public class MovieEmissionService {

    private final static Integer MINIMAL_BREAK_BETWEEN_MOVIE_EMISSIONS_IN_MIN = 10;

    private final MovieEmissionRepository movieEmissionRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final MovieRepository movieRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<MovieEmissionDto> createMovieEmission(CreateMovieEmissionDto createMovieEmission) {

        var errors = new CreateMovieEmissionDtoValidator().validate(createMovieEmission);
        if (Validations.hasErrors(errors)) {
            return Mono.error(new MovieEmissionServiceException(Validations.createErrorMessage(errors)));
        }

        return movieRepository.findById(createMovieEmission.getMovieId())
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
                    return cinemaHallRepository.findById(createMovieEmission.getCinemaHallId())
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
                    return movieEmissionRepository.addOrUpdate(
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
                    return cinemaHallRepository.addOrUpdate(pair.getLeft()).then(Mono.just(pair));
                })
                .map(x -> x.getRight().toDto())
                .as(transactionalOperator::transactional);
    }

    private boolean isFreeSpaceForMovieEmissionInCinemaHall(
            CinemaHall cinemaHall, Movie movie, String startDateTimeOfMovieEmission) {

        var startDateTime = toLocalDateTime(startDateTimeOfMovieEmission);
        var movieEmissionTimesInDay = getMovieEmissionTimesInDay(startDateTime.toLocalDate(), cinemaHall);

        if (movieEmissionTimesInDay.isEmpty()) {
            return true;
        }

        int size = movieEmissionTimesInDay.size();

        /*
         * Replaced peek()+AtomicInteger counter with IntStream.range —
         * peek() is for debugging only; side-effects inside stream operators
         * break referential transparency and are an anti-pattern.
         * First and last intervals get a break margin added; middle intervals keep original bounds.
         */
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
        return movieEmissionRepository
                .findAll()
                .map(MovieEmission::toDto);
    }

    public Flux<MovieEmissionDto> getAllMovieEmissionsByMovieId(String movieId) {
        return movieEmissionRepository
                .findMovieEmissionsByMovieId(movieId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie hall with id: %s".formatted(movieId))))
                .map(MovieEmission::toDto);
    }

    public Flux<MovieEmissionDto> getAllMovieEmissionsByCinemaHallId(String cinemaHallId) {
        return movieEmissionRepository
                .findMovieEmissionsByCinemaHallId(cinemaHallId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No cinema hall with id: %s".formatted(cinemaHallId))))
                .map(MovieEmission::toDto);
    }

    public Mono<MovieEmissionDto> deleteMovieEmission(String movieEmissionId) {
        return movieEmissionRepository
                .deleteById(movieEmissionId)
                .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                        "No movie emission with id: %s".formatted(movieEmissionId))))
                .flatMap(movieEmission -> cinemaHallRepository.getByMovieEmissionId(movieEmissionId)
                        .switchIfEmpty(Mono.error(() -> new MovieEmissionServiceException(
                                "No cinema hall connected to movie emission with id %s".formatted(movieEmissionId))))
                        .flatMap(cinemaHall -> cinemaHallRepository
                                .addOrUpdate(cinemaHall.removeMovieEmissionById(movieEmission.getId()))
                                .then(Mono.just(movieEmission))))
                .map(MovieEmission::toDto)
                .as(transactionalOperator::transactional);
    }
}
