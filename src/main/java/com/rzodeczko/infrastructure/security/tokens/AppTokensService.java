package com.rzodeczko.infrastructure.security.tokens;

import com.rzodeczko.application.exception.AuthenticationException;
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

    public Mono<TokensDto> refreshTokens(String refreshToken) {
        return Mono.fromCallable(() -> extractClaims(refreshToken))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> !(e instanceof AuthenticationException), _ -> new AuthenticationException("Invalid refresh token"))
                .flatMap(claims -> {
                    if (!claims.getExpiration().after(new Date())) {
                        return Mono.error(new AuthenticationException("Refresh token has expired"));
                    }
                    if (!claims.containsKey(jwtProperties.refreshToken().accessTokenKey())) {
                        return Mono.error(new AuthenticationException("Provided token is not a refresh token"));
                    }
                    return Mono.just(claims.getSubject());
                })
                .flatMap(userId -> userPort.findById(userId)
                        .switchIfEmpty(Mono.error(() -> new AuthenticationException("User not found"))))
                .flatMap(user -> Mono.fromCallable(() -> buildTokens(user.getId()))
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