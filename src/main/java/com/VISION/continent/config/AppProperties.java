package com.VISION.continent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String url;

    @Value("${app.frontend.reset-password-path:/reset-password}")
    private String resetPasswordPath;
}