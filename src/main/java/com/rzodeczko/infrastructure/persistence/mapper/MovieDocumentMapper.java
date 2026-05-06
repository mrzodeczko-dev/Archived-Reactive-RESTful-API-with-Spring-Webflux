package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;

public final class MovieDocumentMapper {

    private MovieDocumentMapper() {
    }

    public static MovieDocument toDocument(Movie movie) {
        if (movie == null) {
            return null;
        }
        return new MovieDocument(
                movie.getId(),
                movie.getName(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getPremiereDate());
    }

    public static Movie toDomain(MovieDocument doc) {
        if (doc == null) {
            return null;
        }
        return Movie.builder()
                .id(doc.getId())
                .name(doc.getName())
                .genre(doc.getGenre())
                .duration(doc.getDuration())
                .premiereDate(doc.getPremiereDate())
                .build();
    }
}
