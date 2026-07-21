package com.VISION.continent.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Bean RestTemplate pour les appels HTTP
     * Utilisé par TermiiService et smsService
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}