package com.rzodeczko.infrastructure.security.tokens;

import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.infrastructure.security.dto.TokensDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppTokensService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final UserPort userPort;

    public Mono<TokensDto> generateTokens(User user) {
        if (user == null) {
            return Mono.error(new SecurityException("Generate tokens failed: user is null"));
        }

        return userPort.findByUsername(user.getUsername())
                .switchIfEmpty(Mono.error(new SecurityException(
                        "Generate tokens failed: user not found: " + user.getUsername()
                )))
                .flatMap(userFromDb -> Mono.fromCallable(() -> buildTokens(userFromDb.getId()))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public String getId(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        return extractClaims(token).getExpiration().after(new Date());
    }

    private TokensDto buildTokens(String userId) {
        Date issuedAt = new Date();
        long accessExpirationMillis = System.currentTimeMillis() + jwtProperties.accessToken().expirationTimeMs();
        long refreshExpirationMillis = System.currentTimeMillis() + jwtProperties.refreshToken().expirationTimeMs();

        String accessToken = buildToken(
                String.valueOf(userId),
                new Date(accessExpirationMillis),
                issuedAt,
                Map.of()
        );

        String refreshToken = buildToken(
                String.valueOf(userId),
                new Date(refreshExpirationMillis),
                issuedAt,
                Map.of(jwtProperties.refreshToken().accessTokenKey(), accessExpirationMillis)
        );

        return TokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String buildToken(String subject, Date expiration, Date issuedAt, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}