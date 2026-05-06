package com.rzodeczko.infrastructure.persistence.document;

import com.rzodeczko.domain.security.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;
    private String username;
    private String password;
    private Role role;
    private LocalDate birthDate;
    private List<MovieDocument> favoriteMovies;
    private String email;
}
