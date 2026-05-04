package com.app.application.service;

import com.app.application.dto.CreateMovieDto;
import com.app.application.dto.MovieDto;
import com.app.application.exception.MovieServiceException;
import com.app.application.validator.CreateMovieDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.movie.Movie;
import com.app.domain.movie.MovieRepository;
import com.app.domain.security.User;
import com.app.domain.security.UserRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@RequiredArgsConstructor
@Service
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final CreateMovieDtoValidator createMovieDtoValidator;

    public Flux<MovieDto> getAll() {
        return movieRepository.findAll()
                .filter(Objects::nonNull)
                .map(Movie::toDto);
    }

    public Flux<MovieDto> getFilteredByKeyword(final String keyWord) {
        if (isNull(keyWord)) {
            return Flux.error(() -> new MovieServiceException("Key word is null"));
        }
        return Flux.merge(
                movieRepository.findAllByName(keyWord),
                movieRepository.findAllByGenre(keyWord)
        ).distinct(Movie::getId).map(Movie::toDto);
    }

    public Flux<MovieDto> getFilteredByGenre(final String genre) {
        if (isNull(genre)) {
            return Flux.error(() -> new MovieServiceException("Genre is null"));
        }
        return movieRepository.findAllByGenre(genre).map(Movie::toDto);
    }

    public Flux<MovieDto> getFilteredByName(final String name) {
        if (isNull(name)) {
            return Flux.error(() -> new MovieServiceException("Name is null"));
        }
        return movieRepository.findAllByName(name).map(Movie::toDto);
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
            return movieRepository.findAllByDurationBetween(minDuration, maxDuration).map(Movie::toDto);
        }
        if (!isMinDurationNull) {
            return movieRepository.findAllByDurationGreaterThanEqual(minDuration).map(Movie::toDto);
        }
        return movieRepository.findAllByDurationLessThanEqual(maxDuration).map(Movie::toDto);
    }

    public Flux<MovieDto> getFilteredByPremiereDate(final LocalDate minDate, final LocalDate maxDate) {
        var isMinDateNull = isNull(minDate);
        var isMaxDateNull = isNull(maxDate);

        if ((isMinDateNull && isMaxDateNull) || (!isMinDateNull && !isMaxDateNull && minDate.compareTo(maxDate) > 0)) {
            return Flux.error(() -> new MovieServiceException("At least one boundary date should be defined"));
        }

        if (!isMinDateNull && !isMaxDateNull) {
            return movieRepository.findAllByPremiereDateBetween(minDate, maxDate).map(Movie::toDto);
        }
        if (!isMinDateNull) {
            return movieRepository.findAllByPremiereDateGreaterThanEqual(minDate).map(Movie::toDto);
        }
        return movieRepository.findAllByPremiereDateLessThanEqual(maxDate).map(Movie::toDto);
    }

    public Mono<MovieDto> addMovieToFavorites(final String movieId, final String username) {
        return movieRepository.findById(movieId)
                .switchIfEmpty(Mono.error(() -> new MovieServiceException("No movie with id: %s".formatted(movieId))))
                .flatMap(movie -> userRepository.findByUsername(username)
                        .flatMap(user -> {
                            if (nonNull(user.getFavoriteMovies()) && user.getFavoriteMovies().stream().map(Movie::getId).anyMatch(id -> id.equals(movieId))) {
                                return Mono.error(new MovieServiceException("Movie with id: %s is already in favorites movies".formatted(movieId)));
                            }
                            return Mono.just(user.addMovieToFavorites(movie));
                        })
                        .flatMap(userRepository::addOrUpdate)
                        .then(Mono.just(movie.toDto()))
                );
    }

    public Mono<MovieDto> getById(final String id) {
        return movieRepository.findById(id)
                .map(Movie::toDto);
    }

    public Flux<MovieDto> getFavoriteMovies(String username) {
        return userRepository.findByUsername(username)
                .map(User::getFavoriteMovies)
                .flatMapMany(Flux::fromIterable)
                .map(Movie::toDto);
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
                .flatMap(dto -> movieRepository.addOrUpdate(dto.toEntity()))
                .doOnSuccess(movie -> log.info("Movie {} saved", movie))
                .map(Movie::toDto);
    }

    private Mono<Movie> doMovieExistsInDb(Movie movie, List<String> errorsList, AtomicInteger counter) {
        return movieRepository.findByNameAndGenre(movie.getName(), movie.getGenre())
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
        var errorsList = new ArrayList<String>();
        var counter = new AtomicInteger(1);

        return resourceMono
                .flatMap(resource -> Mono.fromCallable(() -> convertResourceToBufferedReader(resource))
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(bufferedReader -> Mono.fromCallable(() -> collectMoviesToAddFromCsvFile(bufferedReader, errorsList))
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMapIterable(Function.identity())
                .flatMap(movie -> doMovieExistsInDb(movie, errorsList, counter))
                .collectList()
                .map(movies -> saveMovies(movies, errorsList))
                .flatMapMany(Function.identity());
    }

    private Flux<MovieDto> saveMovies(List<Movie> movies, List<String> errorsList) {
        if (!errorsList.isEmpty()) {
            throw new MovieServiceException("Errors are: %s".formatted(errorsList));
        }
        return movieRepository.addOrUpdateMany(movies).map(Movie::toDto);
    }

    private BufferedReader convertResourceToBufferedReader(Resource resource) {
        try {
            return new BufferedReader(new InputStreamReader(resource.getInputStream()));
        } catch (IOException e) {
            throw new MovieServiceException("Cannot parse csv file");
        }
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
        return movieRepository.deleteById(id)
                .map(Movie::toDto);
    }
}
