package com.rzodeczko.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cinemas")
public class CinemaDocument {

    @Id
    private String id;
    private String city;
    private String street;
    private List<CinemaHallDocument> cinemaHalls;
}


