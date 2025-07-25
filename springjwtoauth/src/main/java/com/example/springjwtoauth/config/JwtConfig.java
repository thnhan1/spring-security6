package com.example.springjwtoauth.config;

import com.example.springjwtoauth.service.JwtService;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {
    @Value("${jwtSecret}")
    private String secret;

    @Value("${jwtExpiration}")
    private long expiration;

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtService jwtService(SecretKey secretKey) {
        return new JwtService(secretKey, expiration);
    }
}
