package com.rzodeczko.infrastructure.security;

import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.security.enums.Role;
import com.rzodeczko.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("admin@example.com")
                .password("hashed-pass")
                .role(Role.ROLE_ADMIN)
                .build();
    }

    @Nested
    @DisplayName("findByUsername()")
    class FindByUsernameTests {

        @Test
        @DisplayName("Happy path: domain user mapped to Spring Security user")
        void shouldReturnSpringSecurityUser() {
            when(userPort.findByUsername("admin@example.com")).thenReturn(Mono.just(user));

            StepVerifier.create(appUserDetailsService.findByUsername("admin@example.com"))
                    .assertNext(userDetails -> {
                        assertThat(userDetails.getUsername()).isEqualTo("admin@example.com");
                        assertThat(userDetails.getPassword()).isEqualTo("hashed-pass");
                        assertThat(userDetails.isAccountNonExpired()).isTrue();
                        assertThat(userDetails.isAccountNonLocked()).isTrue();
                        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
                        assertThat(userDetails.isEnabled()).isTrue();
                        assertThat(userDetails.getAuthorities())
                                .extracting("authority")
                                .containsExactly("ROLE_ADMIN");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("User not found: empty Mono")
        void shouldCompleteEmptyWhenUserNotFound() {
            when(userPort.findByUsername("missing@example.com")).thenReturn(Mono.empty());

            StepVerifier.create(appUserDetailsService.findByUsername("missing@example.com"))
                    .verifyComplete();
        }
    }
}
