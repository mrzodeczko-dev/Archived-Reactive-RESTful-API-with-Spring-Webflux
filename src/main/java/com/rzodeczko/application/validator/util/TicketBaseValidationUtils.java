package com.rzodeczko.application.validator.util;

import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.application.dto.contract.TicketDtoMarker;
import com.rzodeczko.domain.vo.Position;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TicketBaseValidationUtils {


    public static boolean isPositionValid(Position position) {
        return nonNull(position) && nonNull(position.getColNo()) && position.getColNo() >= 1
                && nonNull(position.getRowNo()) && position.getRowNo() >= 1;
    }

    public static boolean isMovieEmissionIdValid(String movieEmissionId) {
        return StringUtils.isNotBlank(movieEmissionId);
    }

    public static boolean areTicketDetailsValid(List<TicketDetailsDto> ticketDetails) {
        return nonNull(ticketDetails) && !ticketDetails.isEmpty()
                && arePositionsUnique(ticketDetails.stream().map(TicketDetailsDto::getPosition).collect(Collectors.toList()))
                && ticketDetails.stream().allMatch(TicketBaseValidationUtils::isSingleTicketDetailValid);
    }

    private static boolean isSingleTicketDetailValid(TicketDetailsDto ticketDetailsDto) {
        return nonNull(ticketDetailsDto) && isPositionValid(ticketDetailsDto.getPosition());
    }

    private static boolean arePositionsUnique(List<Position> positions) {

        return positions.stream()
                .distinct().count() == positions.size();
    }

    public static Map<String, String> validate(TicketDtoMarker item) {

        var errors = new HashMap<String, String>();

        if (isNull(item)) {
            errors.put("dto object", "is null");
            return errors;
        }

        if (!isMovieEmissionIdValid(item.getMovieEmissionId())) {
            errors.put("movieEmissionId {%s}".formatted(item.getMovieEmissionId()), "is not valid");
        }

        if (!areTicketDetailsValid(item.getTicketsDetails())) {
            errors.put("ticketDetails {%s}".formatted(item.getTicketsDetails()), "are not valid");
        }

        return errors;

    }


}
