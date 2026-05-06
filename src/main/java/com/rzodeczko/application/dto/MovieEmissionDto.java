package com.rzodeczko.application.dto;

import com.rzodeczko.domain.vo.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MovieEmissionDto {

    private String id;
    private String movieId;
    private LocalDateTime startTime;
    private String cinemaHallId;
    private Map<Position, Boolean> isPositionFree;
//    private List<PositionIndex> positionIndices;
    private String baseTicketPrice;
}
