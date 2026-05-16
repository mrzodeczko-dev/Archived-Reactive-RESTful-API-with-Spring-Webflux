package com.rzodeczko.infrastructure.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretKeyConfigTest {

    private final SecretKeyConfig secretKeyConfig = new SecretKeyConfig();

    @Nested
    @DisplayName("secretKey()")
    class SecretKeyTests {

        @Test
        @DisplayName("Valid Base64 secret: creates HMAC key")
        void shouldCreateSecretKeyFromValidBase64Secret() {
            String secret = Base64.getEncoder().encodeToString(new byte[64]);

            SecretKey secretKey = secretKeyConfig.secretKey(secret);

            assertThat(secretKey.getAlgorithm()).isEqualTo("HmacSHA512");
            assertThat(secretKey.getEncoded()).hasSize(64);
        }

        @Test
        @DisplayName("Blank secret: IllegalStateException")
        void shouldThrowWhenSecretIsBlank() {
            assertThatThrownBy(() -> secretKeyConfig.secretKey(" "))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("jwt.secret-key is not set");
        }

        @Test
        @DisplayName("Too short decoded secret: IllegalStateException with decoded length")
        void shouldThrowWhenDecodedSecretIsTooShort() {
            String secret = Base64.getEncoder().encodeToString(new byte[32]);

            assertThatThrownBy(() -> secretKeyConfig.secretKey(secret))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("need >= 64 decoded bytes")
                    .hasMessageContaining("got 32");
        }
    }
}
