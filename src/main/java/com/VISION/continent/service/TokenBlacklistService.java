package com.VISION.continent.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Liste noire des JWT revoques (logout), stockee dans Redis.
 *
 * Redis est ici un CONFORT, pas une dependance vitale : s'il est indisponible,
 * on log un avertissement et on laisse passer. Sans cela, une panne Redis
 * transformait toutes les routes protegees en 403.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "jwt:blacklist:";

    public void revoquer(String jti, Duration dureeRestante) {
        if (jti == null || dureeRestante == null
                || dureeRestante.isNegative() || dureeRestante.isZero()) {
            return; // deja expire, inutile de stocker
        }
        try {
            redisTemplate.opsForValue().set(PREFIX + jti, "revoked", dureeRestante);
        } catch (RuntimeException e) {
            log.warn("Redis indisponible : le token {} n'a pas pu etre revoque ({})",
                    jti, e.getMessage());
        }
    }

    public boolean estRevoque(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
        } catch (RuntimeException e) {
            log.warn("Redis indisponible : verification de revocation ignoree ({})",
                    e.getMessage());
            return false;
        }
    }
}
