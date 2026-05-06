package com.rzodeczko.infrastructure.security;

import com.rzodeczko.application.port.out.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService {
    protected final UserPort userPort;

    public Mono<User> findByUsername(String username) {
        return userPort
                .findByUsername(username)
                .map(user -> new User(
                        user.getUsername(),
                        user.getPassword(),
                        true, true, true, true,
                        List.of(new SimpleGrantedAuthority(user.getRole().toString()))
                ));
    }
}
