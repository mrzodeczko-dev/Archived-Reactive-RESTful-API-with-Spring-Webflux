package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

public interface MongoMovieEmissionRepository extends ReactiveMongoRepository<MovieEmissionDocument, String> {

    Flux<MovieEmissionDocument> findMovieEmissionsByMovieId(String movieId);

    Flux<MovieEmissionDocument> findMovieEmissionsByCinemaHallId(String cinemaHallId);

    @Query(value = "{'cinemaHallId': {$in: ?0}, 'startDateTime': {$gt: ?1, $lt: ?2}, 'movieId': ?3}")
    Flux<MovieEmission> findMovieEmissionsByCinemaHallIdInAndStartDateTimeBetweenAndMovieId(List<String> cinemaHallIds, LocalDateTime fromDate, LocalDateTime toDate, String movieId);
}
