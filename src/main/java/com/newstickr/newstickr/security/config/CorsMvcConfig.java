package com.newstickr.newstickr.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie", "Authorization")
                .allowedOrigins(
                        "http://localhost",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173"
                );
    }
}
