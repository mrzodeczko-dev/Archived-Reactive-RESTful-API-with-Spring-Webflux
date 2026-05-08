package com.rzodeczko.application.dto;

import com.rzodeczko.application.service.util.ServiceUtils;
import com.rzodeczko.domain.cinema_hall.CinemaHall;

import java.util.ArrayList;

public record CreateCinemaHallDto(
        Integer rowNo,
        Integer colNo
) {
    public CinemaHall toEntity(String cinemaId) {
        return CinemaHall.builder()
                .cinemaId(cinemaId)
                .movieEmissions(new ArrayList<>())
                .positions(ServiceUtils.buildPositions(rowNo, colNo))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer rowNo;
        private Integer colNo;

        public Builder rowNo(Integer rowNo) {
            this.rowNo = rowNo;
            return this;
        }

        public Builder colNo(Integer colNo) {
            this.colNo = colNo;
            return this;
        }

        public CreateCinemaHallDto build() {
            return new CreateCinemaHallDto(rowNo, colNo);
        }
    }
}