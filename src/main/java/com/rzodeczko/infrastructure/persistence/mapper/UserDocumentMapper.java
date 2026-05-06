package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.domain.security.BaseUser;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.domain.security.enums.Role;
import com.rzodeczko.infrastructure.persistence.document.MovieDocument;
import com.rzodeczko.infrastructure.persistence.document.UserDocument;

import java.util.List;
import java.util.stream.Collectors;

public final class UserDocumentMapper {

    private UserDocumentMapper() {
    }

    public static UserDocument toDocument(User u) {
        if (u == null) return null;
        UserDocument doc = new UserDocument();
        doc.setId(u.getId());
        doc.setUsername(u.getUsername());
        doc.setPassword(u.getPassword());
        doc.setRole(u.getRole());
        doc.setBirthDate(u.getBirthDate());
        doc.setEmail(u.getEmail());
        doc.setFavoriteMovies(toDocs(u.getFavoriteMovies()));
        return doc;
    }

    public static UserDocument toDocument(Admin a) {
        if (a == null) return null;
        UserDocument doc = new UserDocument();
        doc.setId(a.getId());
        doc.setUsername(a.getUsername());
        doc.setPassword(a.getPassword());
        doc.setRole(a.getRole());
        return doc;
    }

    public static UserDocument toDocumentFromBase(BaseUser bu) {
        if (bu instanceof User u) return toDocument(u);
        if (bu instanceof Admin a) return toDocument(a);
        return null;
    }

    public static User toUserDomain(UserDocument doc) {
        if (doc == null) return null;
        User u = User.builder()
                .username(doc.getUsername())
                .password(doc.getPassword())
                .email(doc.getEmail())
                .birthDate(doc.getBirthDate())
                .build();
        u.setId(doc.getId());
        if (doc.getFavoriteMovies() != null) {
            for (Movie m : toDomainMovies(doc.getFavoriteMovies())) {
                u.addMovieToFavorites(m);
            }
        }
        return u;
    }

    public static Admin toAdminDomain(UserDocument doc) {
        if (doc == null) return null;
        Admin a = new Admin(doc.getUsername(), doc.getPassword());
        a.setId(doc.getId());
        return a;
    }

    public static BaseUser toBaseUserDomain(UserDocument doc) {
        if (doc == null) return null;
        if (doc.getRole() == Role.ROLE_ADMIN) return toAdminDomain(doc);
        return toUserDomain(doc);
    }

    private static List<MovieDocument> toDocs(List<Movie> movies) {
        if (movies == null) return null;
        return movies.stream().map(MovieDocumentMapper::toDocument).collect(Collectors.toList());
    }

    private static List<Movie> toDomainMovies(List<MovieDocument> docs) {
        if (docs == null) return null;
        return docs.stream().map(MovieDocumentMapper::toDomain).collect(Collectors.toList());
    }
}
