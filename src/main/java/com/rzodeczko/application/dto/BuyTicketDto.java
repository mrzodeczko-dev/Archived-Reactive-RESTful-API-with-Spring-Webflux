package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BuyTicketDto {

    private String cityId;
    private String cinemaId;
    private String movieId;
    private LocalDateTime dateTime;
    private List<TicketDetailsDto> ticketsDetails;
}
