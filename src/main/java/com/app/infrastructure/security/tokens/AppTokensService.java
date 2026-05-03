package com.app.infrastructure.security.tokens;

import com.app.domain.security.UserRepository;
import com.app.infrastructure.security.dto.TokensDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AppTokensService {

    @Value("${jwt.access-token.expiration-time-ms}")
    private Long accessTokenExpirationTimeInMs;

    @Value("${jwt.refresh-token.expiration-time-ms}")
    private Long refreshTokenExpirationTimeInMs;

    @Value("${jwt.token.prefix}")
    private String jwtTokenPrefix;

    @Value("${jwt.refresh-token.access-token-key}")
    private String refreshTokenAccessTokenKey;

    private final SecretKey secretKey;
    private final UserRepository userRepository;

    public Mono<TokensDto> generateTokens(User user) {
        // Fixed: was throwing synchronously before reactive chain.
        // Mono.error() properly propagates the error in the reactive pipeline.
        if (user == null) {
            return Mono.error(() -> new SecurityException("generate tokens - authentication object is null"));
        }

        return userRepository
                .findByUsername(user.getUsername())
                .flatMap(userFromDb -> {
                    var id = userFromDb.getId();
                    var createdDate = new Date();
                    var accessTokenExpirationTimeMillis = System.currentTimeMillis() + accessTokenExpirationTimeInMs;
                    var accessTokenExpirationTime = new Date(accessTokenExpirationTimeMillis);
                    var refreshTokenExpirationTime = new Date(System.currentTimeMillis() + refreshTokenExpirationTimeInMs);

                    var accessToken = Jwts
                            .builder()
                            .setSubject(String.valueOf(id))
                            .setExpiration(accessTokenExpirationTime)
                            .setIssuedAt(createdDate)
                            .signWith(secretKey)
                            .compact();

                    var refreshToken = Jwts
                            .builder()
                            .setSubject(String.valueOf(id))
                            .setExpiration(refreshTokenExpirationTime)
                            .setIssuedAt(createdDate)
                            .signWith(secretKey)
                            .claim(refreshTokenAccessTokenKey, accessTokenExpirationTimeMillis)
                            .compact();

                    return Mono.just(TokensDto
                            .builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build());
                });
    }

    private Claims claims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getId(String token) {
        return claims(token.substring(7)).getSubject();
    }

    private Date getExpiration(String token) {
        return claims(token).getExpiration();
    }

    public boolean isTokenValid(String token) {
        Date expirationDate = getExpiration(token.substring(7));
        return expirationDate.after(new Date());
    }
}
