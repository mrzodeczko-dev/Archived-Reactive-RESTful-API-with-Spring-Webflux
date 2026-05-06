package com.rzodeczko.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class MovieDocument {

    @Id
    private String id;
    private String name;
    private String genre;
    private Integer duration;
    private LocalDate premiereDate;
}
