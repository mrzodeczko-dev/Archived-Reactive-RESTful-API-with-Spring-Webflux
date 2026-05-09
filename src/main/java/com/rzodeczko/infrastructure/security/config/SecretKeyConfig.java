package com.rzodeczko.infrastructure.security.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class SecretKeyConfig {

    /**
     * Base64-encoded HMAC key for JWT signing. Must be at least 64 bytes (512 bits) decoded for HS512.
     * Pass through env var: JWT_SECRET_KEY (-> jwt.secret-key in Spring config).
     * Generate with:  openssl rand -base64 96
     */
    @Bean
    public SecretKey secretKey(@Value("${jwt.secret-key}") String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException(
                    "Property jwt.secret-key is not set. Provide a Base64-encoded HMAC key (>= 64 bytes for HS512).");
        }
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "jwt.secret-key is too short for HS512 (need >= 64 decoded bytes, got " + keyBytes.length + ").");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}