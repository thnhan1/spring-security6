package com.nhanab.accountservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(token, "revoked", expirationMillis, TimeUnit.MILLISECONDS);
    }
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
