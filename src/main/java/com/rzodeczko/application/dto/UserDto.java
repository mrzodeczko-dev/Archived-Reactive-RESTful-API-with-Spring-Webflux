package com.rzodeczko.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserDto {

    private String id;
    private String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String birthDate;

    private String role;

    private String email;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MovieDto> favoriteMovies;

}
