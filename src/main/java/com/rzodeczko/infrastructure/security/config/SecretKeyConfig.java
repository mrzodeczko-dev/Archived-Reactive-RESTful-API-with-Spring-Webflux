package com.rzodeczko.infrastructure.security.config;

import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class SecretKeyConfig {
    @Bean
    public SecretKey secretKey() {
        return Jwts.SIG.HS512.key().build();
    }
}
