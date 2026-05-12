package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvBindByName;
import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.util.Strings.isBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CsvCinemaRow {

    private static final Pattern HALL_CAPACITY_PATTERN = Pattern.compile("\\d+x\\d+");

    @CsvBindByName
    private String city;

    @CsvBindByName
    private String street;

    @CsvBindByName
    private String cinemaHalls;

    public CreateCinemaDto toApplicationDto(List<String> errorsList, int rowNo) {
        return new CreateCinemaDto(city, street, parseCinemaHalls(errorsList, rowNo));
    }

    private List<CreateCinemaHallDto> parseCinemaHalls(List<String> errorsList, int rowNo) {
        if (isBlank(cinemaHalls)) {
            errorsList.add("Cinema in row no. %s has blank cinemaHalls".formatted(rowNo));
            return List.of();
        }

        var hallCounter = new AtomicInteger(1);
        return Arrays.stream(cinemaHalls.split("\\|"))
                .map(String::trim)
                .map(value -> parseCinemaHall(value, rowNo, hallCounter.getAndIncrement(), errorsList))
                .filter(Objects::nonNull)
                .toList();
    }

    private CreateCinemaHallDto parseCinemaHall(String value, int rowNo, int hallNo, List<String> errorsList) {
        if (!HALL_CAPACITY_PATTERN.matcher(value).matches()) {
            errorsList.add("Cinema in row no. %s has invalid hall no. %s: %s. Expected format is rowNo x colNo, e.g. 10x12"
                    .formatted(rowNo, hallNo, value));
            return null;
        }

        var parts = value.split("x");
        return new CreateCinemaHallDto(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
