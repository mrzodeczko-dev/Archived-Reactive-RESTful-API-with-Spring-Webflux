package com.rzodeczko.infrastructure.config;

import com.rzodeczko.infrastructure.persistence.initscripts.AppAdminCredentials;
import com.rzodeczko.infrastructure.security.tokens.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, AppAdminCredentials.class})
public class AppConfigurationProperties {
}
