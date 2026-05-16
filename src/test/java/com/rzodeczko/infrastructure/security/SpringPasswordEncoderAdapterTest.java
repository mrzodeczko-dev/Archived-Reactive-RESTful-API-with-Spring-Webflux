package com.rzodeczko.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringPasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SpringPasswordEncoderAdapter adapter;

    @Nested
    @DisplayName("encode()")
    class EncodeTests {

        @Test
        @DisplayName("Delegates raw password encoding")
        void shouldDelegateEncode() {
            when(passwordEncoder.encode("Secret123!")).thenReturn("{bcrypt}encoded");

            assertThat(adapter.encode("Secret123!")).isEqualTo("{bcrypt}encoded");

            verify(passwordEncoder).encode("Secret123!");
        }
    }

    @Nested
    @DisplayName("matches()")
    class MatchesTests {

        @Test
        @DisplayName("Delegates positive password match")
        void shouldDelegatePositiveMatch() {
            when(passwordEncoder.matches("Secret123!", "{bcrypt}encoded")).thenReturn(true);

            assertThat(adapter.matches("Secret123!", "{bcrypt}encoded")).isTrue();

            verify(passwordEncoder).matches("Secret123!", "{bcrypt}encoded");
        }

        @Test
        @DisplayName("Delegates negative password match")
        void shouldDelegateNegativeMatch() {
            when(passwordEncoder.matches("wrong", "{bcrypt}encoded")).thenReturn(false);

            assertThat(adapter.matches("wrong", "{bcrypt}encoded")).isFalse();

            verify(passwordEncoder).matches("wrong", "{bcrypt}encoded");
        }
    }
}
