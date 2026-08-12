package com.VISION.continent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "jwt:blacklist:";

    public void revoquer(String jti, Duration dureeRestante) {
        if (dureeRestante.isNegative() || dureeRestante.isZero()) {
            return; // déjà expiré, inutile de stocker
        }
        redisTemplate.opsForValue().set(PREFIX + jti, "revoked", dureeRestante);
    }

    public boolean estRevoque(String jti) {
        return redisTemplate.hasKey(PREFIX + jti);
    }
}