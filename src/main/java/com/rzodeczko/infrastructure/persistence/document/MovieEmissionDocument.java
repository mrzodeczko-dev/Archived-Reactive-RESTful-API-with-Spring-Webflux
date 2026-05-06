package com.rzodeczko.infrastructure.persistence.document;

import com.rzodeczko.domain.vo.Money;
import com.rzodeczko.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movie_emissions")
public class MovieEmissionDocument {

    @Id
    private String id;
    private MovieDocument movie;
    private LocalDateTime startDateTime;
    private Money baseTicketPrice;
    private String cinemaHallId;
    private Map<Position, Boolean> isPositionFree;
}
