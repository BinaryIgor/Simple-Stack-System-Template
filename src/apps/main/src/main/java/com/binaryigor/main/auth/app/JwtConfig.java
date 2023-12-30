package com.binaryigor.main.auth.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

//TODO: maybe rotation?
@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(String issuer,
                        String tokenKey,
                        Duration tokenDuration) {
}
