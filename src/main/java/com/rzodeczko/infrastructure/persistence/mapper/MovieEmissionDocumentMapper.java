package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.movie_emission.MovieEmission;
import com.rzodeczko.infrastructure.persistence.document.MovieEmissionDocument;

import java.util.List;
import java.util.stream.Collectors;

public final class MovieEmissionDocumentMapper {

    private MovieEmissionDocumentMapper() {
    }

    public static MovieEmissionDocument toDocument(MovieEmission me) {
        if (me == null) {
            return null;
        }
        return new MovieEmissionDocument(
                me.getId(),
                MovieDocumentMapper.toDocument(me.getMovie()),
                me.getStartDateTime(),
                me.getBaseTicketPrice(),
                me.getCinemaHallId(),
                me.getIsPositionFree());
    }

    public static MovieEmission toDomain(MovieEmissionDocument doc) {
        if (doc == null) {
            return null;
        }
        return MovieEmission.builder()
                .id(doc.getId())
                .movie(MovieDocumentMapper.toDomain(doc.getMovie()))
                .startDateTime(doc.getStartDateTime())
                .baseTicketPrice(doc.getBaseTicketPrice())
                .cinemaHallId(doc.getCinemaHallId())
                .isPositionFree(doc.getIsPositionFree())
                .build();
    }

    public static List<MovieEmissionDocument> toDocuments(List<MovieEmission> list) {
        if (list == null) return null;
        return list.stream().map(MovieEmissionDocumentMapper::toDocument).collect(Collectors.toList());
    }

    public static List<MovieEmission> toDomains(List<MovieEmissionDocument> list) {
        if (list == null) return null;
        return list.stream().map(MovieEmissionDocumentMapper::toDomain).collect(Collectors.toList());
    }
}
