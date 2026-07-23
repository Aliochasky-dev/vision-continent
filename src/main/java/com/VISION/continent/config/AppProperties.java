package com.VISION.continent.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.frontend")
public class AppProperties {

    private String url = "http://localhost:8080";
    private String resetPasswordPath = "/reset-password";
}