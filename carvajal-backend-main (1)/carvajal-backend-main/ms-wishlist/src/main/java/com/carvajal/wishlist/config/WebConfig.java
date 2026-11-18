package com.carvajal.wishlist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS configurable por variable de entorno CORS_ORIGINS (antes estaba fijo a localhost:4200). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public WebConfig(@Value("${cors.allowed-origins:http://localhost:4200}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS");
    }
}
