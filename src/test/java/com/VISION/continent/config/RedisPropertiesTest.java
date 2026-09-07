package com.VISION.continent.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifie la resolution des proprietes Redis de application.properties
 * avec les variables d'environnement reellement definies sur Render.
 */
class RedisPropertiesTest {

    private StandardEnvironment environnement(Map<String, Object> variablesEnv) throws IOException {
        StandardEnvironment env = new StandardEnvironment();
        // Les variables "d'environnement" simulees passent AVANT le fichier.
        env.getPropertySources().addFirst(new MapPropertySource("env-simule", variablesEnv));
        env.getPropertySources().addLast(
                new ResourcePropertySource("classpath:application.properties"));
        return env;
    }

    @Test
    @DisplayName("Sans variable : repli sur localhost, l'app demarre en local")
    void repliSurLocalhost() throws IOException {
        StandardEnvironment env = environnement(Map.of());

        assertEquals("localhost", env.getProperty("spring.data.redis.host"));
        assertEquals("6379", env.getProperty("spring.data.redis.port"));
    }

    @Test
    @DisplayName("Les variables Render pointent bien sur le service vision-redis")
    void variablesDeRender() throws IOException {
        StandardEnvironment env = environnement(Map.of(
                "REDIS_HOST", "red-da7volgae00c73a9oi9g",
                "REDIS_PORT", "6379"));

        assertEquals("red-da7volgae00c73a9oi9g", env.getProperty("spring.data.redis.host"));
        assertEquals("6379", env.getProperty("spring.data.redis.port"));
    }

    @Test
    @DisplayName("REDIS_PASSWORD est pris en compte s'il est defini")
    void motDePasseRedisEstPrisEnCompte() throws IOException {
        StandardEnvironment env = environnement(Map.of("REDIS_PASSWORD", "secret-redis"));

        assertEquals("secret-redis", env.getProperty("spring.data.redis.password"));
    }

    @Test
    @DisplayName("Les timeouts sont bornes a 2s, pas les 60s par defaut de Lettuce")
    void timeoutsCourts() throws IOException {
        StandardEnvironment env = environnement(Map.of());

        assertEquals("2000ms", env.getProperty("spring.data.redis.timeout"));
        assertEquals("2000ms", env.getProperty("spring.data.redis.connect-timeout"));
    }
}
