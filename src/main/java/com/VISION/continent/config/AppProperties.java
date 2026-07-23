package com.VISION.continent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.frontend")
public class AppProperties {

    private String url = "http://localhost:5173";
    private String resetPasswordPath = "/reset-password";
}