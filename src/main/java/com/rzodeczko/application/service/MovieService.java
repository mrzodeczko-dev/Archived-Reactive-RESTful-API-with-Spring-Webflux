package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMovieDto;
import com.rzodeczko.application.dto.MovieDto;
import com.rzodeczko.application.exception.MovieServiceException;
import com.rzodeczko.application.mapper.MovieMapper;
import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.validator.CreateMovieDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.security.User;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MovieService {

    private static final Logger log = LogManager.getLogger(MovieService.class);

    private final MoviePort moviePort;
    private final UserPort userPort;
    private final CreateMovieDtoValidator createMovieDtoValidator;

    public MovieService(MoviePort moviePort, UserPort userPort, CreateMovieDtoValidator createMovieDtoValidator) {
        this.moviePort = moviePort;
        this.userPort = userPort;
        this.createMovieDtoValidator = createMovieDtoValidator;
    }

    public Flux<MovieDto> getAll() {
        return moviePort.findAll()
                .filter(Objects::nonNull)
                .map(MovieMapper::toDto);
    }

    public Flux<MovieDto> getFilteredByKeyword(final String keyWord) {
        if (isNull(keyWord)) {
            return Flux.error(() -> new MovieServiceException("Key word is null"));
        }
        return Flux.merge(
                moviePort.findAllByName(keyWord),
                moviePort.findAllByGenre(keyWord)
        ).distinct(Movie::getId).map(MovieMapper::toDto);
    }

    public Flux<MovieDto> getFilteredByGenre(final String genre) {
        if (isNull(genre)) {
            return Flux.error(() -> new MovieServiceException("Genre is null"));
        }
        return moviePort.findAllByGenre(genre).map(MovieMapper::toDto);
    }

    public Flux<MovieDto> getFilteredByName(final String name) {
        if (isNull(name)) {
            return Flux.error(() -> new MovieServiceException("Name is null"));
        }
        return moviePort.findAllByName(name).map(MovieMapper::toDto);
    }

    public Flux<MovieDto> getFilteredByDuration(final Integer minDuration, final Integer maxDuration) {
        var isMinDurationNull = isNull(minDuration);
        var isMaxDurationNull = isNull(maxDuration);

        if ((isMinDurationNull && isMaxDurationNull) ||
                (!isMinDurationNull && minDuration <= 0) ||
                (!isMaxDurationNull && maxDuration <= 0) ||
                (!isMinDurationNull && !isMaxDurationNull && minDuration > maxDuration)
        ) {
            return Flux.error(() -> new MovieServiceException(
                    """
                            Movie duration is not set correctly!

                            Conditions to met:
                            1) At least one boundary movie duration should be set,
                            2) Variable minDuration must not be greater than maxDuration (if defined),
                            3) Variables minDuration and maxDuration should be positive numbers (if both are defined)
                            """));
        }

        if (!isMinDurationNull && !isMaxDurationNull) {
            return moviePort.findAllByDurationBetween(minDuration, maxDuration).map(MovieMapper::toDto);
        }
        if (!isMinDurationNull) {
            return moviePort.findAllByDurationGreaterThanEqual(minDuration).map(MovieMapper::toDto);
        }
        return moviePort.findAllByDurationLessThanEqual(maxDuration).map(MovieMapper::toDto);
    }

    public Flux<MovieDto> getFilteredByPremiereDate(final LocalDate minDate, final LocalDate maxDate) {
        var isMinDateNull = isNull(minDate);
        var isMaxDateNull = isNull(maxDate);

        if ((isMinDateNull && isMaxDateNull) || (!isMinDateNull && !isMaxDateNull && minDate.compareTo(maxDate) > 0)) {
            return Flux.error(() -> new MovieServiceException("At least one boundary date should be defined"));
        }

        if (!isMinDateNull && !isMaxDateNull) {
            return moviePort.findAllByPremiereDateBetween(minDate, maxDate).map(MovieMapper::toDto);
        }
        if (!isMinDateNull) {
            return moviePort.findAllByPremiereDateGreaterThanEqual(minDate).map(MovieMapper::toDto);
        }
        return moviePort.findAllByPremiereDateLessThanEqual(maxDate).map(MovieMapper::toDto);
    }

    public Mono<MovieDto> addMovieToFavorites(final String movieId, final String username) {
        return moviePort.findById(movieId)
                .switchIfEmpty(Mono.error(() -> new MovieServiceException("No movie with id: %s".formatted(movieId))))
                .flatMap(movie -> userPort.findByUsername(username)
                        .flatMap(user -> {
                            if (nonNull(user.getFavoriteMovies()) && user.getFavoriteMovies().stream().map(Movie::getId).anyMatch(id -> id.equals(movieId))) {
                                return Mono.error(new MovieServiceException("Movie with id: %s is already in favorites movies".formatted(movieId)));
                            }
                            return Mono.just(user.addMovieToFavorites(movie));
                        })
                        .flatMap(userPort::addOrUpdate)
                        .then(Mono.just(MovieMapper.toDto(movie)))
                );
    }

    public Mono<MovieDto> getById(final String id) {
        return moviePort.findById(id)
                .map(MovieMapper::toDto);
    }

    public Flux<MovieDto> getFavoriteMovies(String username) {
        return userPort.findByUsername(username)
                .map(User::getFavoriteMovies)
                .flatMapMany(Flux::fromIterable)
                .map(MovieMapper::toDto);
    }

    public Mono<MovieDto> addMovie(final Mono<CreateMovieDto> createMovieDto) {
        return createMovieDto
                .map(dto -> {
                    var errors = createMovieDtoValidator.validate(dto);
                    if (Validations.hasErrors(errors)) {
                        throw new MovieServiceException(Validations.createErrorMessage(errors));
                    }
                    return dto;
                })
                .flatMap(dto -> moviePort.addOrUpdate(dto.toEntity()))
                .doOnSuccess(movie -> log.info("Movie {} saved", movie))
                .map(MovieMapper::toDto);
    }

    private Mono<Movie> doMovieExistsInDb(Movie movie, List<String> errorsList, AtomicInteger counter) {
        return moviePort.findByNameAndGenre(movie.getName(), movie.getGenre())
                .filter(Objects::nonNull)
                .hasElement()
                .map(isPresent -> {
                    var counterVal = counter.getAndIncrement();
                    if (isPresent) {
                        errorsList.add("Movie in row no. %s is not unique by name nad genre".formatted(counterVal));
                    }
                    return movie;
                })
                .then(Mono.just(movie));
    }

    public Flux<MovieDto> uploadCSVFile(final Mono<Resource> resourceMono) {
        var errorsList = Collections.synchronizedList(new java.util.ArrayList<String>());
        var counter = new AtomicInteger(1);

        return resourceMono
                .flatMapMany(resource ->
                        Flux.using(
                                () -> new BufferedReader(new InputStreamReader(resource.getInputStream())),
                                bufferedReader -> Mono.fromCallable(() -> collectMoviesToAddFromCsvFile(bufferedReader, errorsList))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .flatMapIterable(Function.identity())
                                        .flatMap(movie -> doMovieExistsInDb(movie, errorsList, counter))
                                        .collectList()
                                        .flatMap(movies -> saveMovies(movies, errorsList))
                                        .flatMapMany(Function.identity()),
                                bufferedReader -> {
                                    try {
                                        bufferedReader.close();
                                    } catch (IOException e) {
                                        log.warn("Failed to close BufferedReader", e);
                                    }
                                }
                        )
                );
    }

    private Mono<Flux<MovieDto>> saveMovies(List<Movie> movies, List<String> errorsList) {
        if (!errorsList.isEmpty()) {
            return Mono.error(new MovieServiceException("Errors are: %s".formatted(errorsList)));
        }
        return Mono.just(moviePort.addOrUpdateMany(movies).map(MovieMapper::toDto));
    }

    private List<Movie> collectMoviesToAddFromCsvFile(BufferedReader bufferedReader, List<String> errorsList) {
        try {
            var counter = new AtomicInteger(1);

            return new CsvToBeanBuilder<CreateMovieDto>(bufferedReader)
                    .withType(CreateMovieDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .sequential()
                    .peek(dto -> {
                        var errors = createMovieDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();

                        if (Validations.hasErrors(errors)) {
                            errorsList.add("Movie in row no. %s is not valid. %s".formatted(counterVal, Validations.createErrorMessage(errors)));
                        }
                    })
                    .map(CreateMovieDto::toEntity)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw e instanceof MovieServiceException me ? me : new MovieServiceException("The file extension .csv is required");
        }
    }

    public Mono<MovieDto> deleteMovieById(final String id) {
        return moviePort.deleteById(id)
                .map(MovieMapper::toDto);
    }
}
