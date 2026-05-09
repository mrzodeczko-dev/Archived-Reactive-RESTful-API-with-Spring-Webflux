package com.rzodeczko.infrastructure.security.tokens;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for {@code jwt.*} properties from application.yml.
 * The secret key itself lives in {@link com.rzodeczko.infrastructure.security.config.SecretKeyConfig}
 * because it is consumed only there.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        AccessToken accessToken,
        RefreshToken refreshToken,
        Token token
) {
    public record AccessToken(long expirationTimeMs) {}
    public record RefreshToken(long expirationTimeMs, String accessTokenKey) {}
    public record Token(String prefix) {}
}