package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.cinema.Cinema;
import com.rzodeczko.infrastructure.persistence.document.CinemaDocument;

import java.util.List;
import java.util.stream.Collectors;

public final class CinemaDocumentMapper {

    private CinemaDocumentMapper() {
    }

    public static CinemaDocument toDocument(Cinema c) {
        if (c == null) return null;
        return new CinemaDocument(
                c.getId(),
                c.getCity(),
                c.getStreet(),
                CinemaHallDocumentMapper.toDocuments(c.getCinemaHalls()));
    }

    public static Cinema toDomain(CinemaDocument doc) {
        if (doc == null) return null;
        return Cinema.builder()
                .id(doc.getId())
                .city(doc.getCity())
                .street(doc.getStreet())
                .cinemaHalls(CinemaHallDocumentMapper.toDomains(doc.getCinemaHalls()))
                .build();
    }

    public static List<CinemaDocument> toDocuments(List<Cinema> list) {
        if (list == null) return null;
        return list.stream().map(CinemaDocumentMapper::toDocument).collect(Collectors.toList());
    }

    public static List<Cinema> toDomains(List<CinemaDocument> list) {
        if (list == null) return null;
        return list.stream().map(CinemaDocumentMapper::toDomain).collect(Collectors.toList());
    }
}
