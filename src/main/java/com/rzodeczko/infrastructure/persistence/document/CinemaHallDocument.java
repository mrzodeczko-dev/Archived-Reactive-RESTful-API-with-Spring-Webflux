package com.rzodeczko.infrastructure.persistence.document;

import com.rzodeczko.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cinema_halls")
public class CinemaHallDocument {

    @Id
    private String id;
    private List<Position> positions;
    private String cinemaId;
    private List<MovieEmissionDocument> movieEmissions;
}
