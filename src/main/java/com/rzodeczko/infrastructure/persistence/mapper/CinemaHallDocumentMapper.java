package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.infrastructure.persistence.document.CinemaHallDocument;

import java.util.List;
import java.util.stream.Collectors;

public final class CinemaHallDocumentMapper {

    private CinemaHallDocumentMapper() {
    }

    public static CinemaHallDocument toDocument(CinemaHall ch) {
        if (ch == null) {
            return null;
        }
        return new CinemaHallDocument(
                ch.getId(),
                ch.getPositions(),
                ch.getCinemaId(),
                ch.getMovieEmissions() == null ? null :
                        ch.getMovieEmissions().stream()
                                .map(MovieEmissionDocumentMapper::toDocument)
                                .collect(Collectors.toList()));
    }

    public static CinemaHall toDomain(CinemaHallDocument doc) {
        if (doc == null) {
            return null;
        }
        return CinemaHall.builder()
                .id(doc.getId())
                .positions(doc.getPositions())
                .cinemaId(doc.getCinemaId())
                .movieEmissions(doc.getMovieEmissions() == null ? null :
                        doc.getMovieEmissions().stream()
                                .map(MovieEmissionDocumentMapper::toDomain)
                                .collect(Collectors.toList()))
                .build();
    }

    public static List<CinemaHallDocument> toDocuments(List<CinemaHall> list) {
        if (list == null) return null;
        return list.stream().map(CinemaHallDocumentMapper::toDocument).collect(Collectors.toList());
    }

    public static List<CinemaHall> toDomains(List<CinemaHallDocument> list) {
        if (list == null) return null;
        return list.stream().map(CinemaHallDocumentMapper::toDomain).collect(Collectors.toList());
    }
}
