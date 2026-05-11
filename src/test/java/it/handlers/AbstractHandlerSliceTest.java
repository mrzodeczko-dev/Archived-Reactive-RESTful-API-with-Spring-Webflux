package it.handlers;

import com.rzodeczko.CinemaApplication;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.infrastructure.config.AppConfigurationProperties;
import com.rzodeczko.infrastructure.config.ApplicationBeansConfig;
import com.rzodeczko.infrastructure.persistence.repository.impl.UserRepositoryImpl;
import com.rzodeczko.infrastructure.security.AuthenticationManager;
import com.rzodeczko.infrastructure.security.SecurityContextRepository;
import com.rzodeczko.infrastructure.security.config.SecretKeyConfig;
import com.rzodeczko.infrastructure.security.tokens.AppTokensService;
import com.rzodeczko.infrastructure.security.tokens.JwtProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Marker / common bits for slice tests of routing + handler beans.
 *
 * <p>The nested {@link Configs} is intended to be {@code @Import}-ed by each
 * handler slice test so the production {@code WebSecurityConfig} (which requires JWT auth)
 * does not block requests. Slice tests verify HTTP routing + status codes + body shape;
 * authorization is its own concern and tested separately.
 */
public abstract class AbstractHandlerSliceTest {

    @TestConfiguration
    @Import({
            ServerHttpSecurity.class,
    })
    public static class Configs {

        @Bean
        public DataBufferFactory dataBufferFactory() {
            return new DefaultDataBufferFactory();
        }

        @Bean
        public SecurityWebFilterChain noOpFilterChain(ServerHttpSecurity serverHttpSecurity) {
            return serverHttpSecurity
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                    .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                    .build();
        }
    }
}