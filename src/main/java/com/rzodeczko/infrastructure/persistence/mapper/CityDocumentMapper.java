package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.city.City;
import com.rzodeczko.infrastructure.persistence.document.CityDocument;

public final class CityDocumentMapper {

    private CityDocumentMapper() {
    }

    public static CityDocument toDocument(City c) {
        if (c == null) return null;
        return new CityDocument(
                c.getId(),
                c.getName(),
                CinemaDocumentMapper.toDocuments(c.getCinemas()));
    }

    public static City toDomain(CityDocument doc) {
        if (doc == null) return null;
        return City.builder()
                .id(doc.getId())
                .name(doc.getName())
                .cinemas(CinemaDocumentMapper.toDomains(doc.getCinemas()))
                .build();
    }
}
