package com.becerra.uss.simulador.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configurable. En desarrollo el valor por defecto "*" permite cualquier origen; en producción defina por ejemplo
 * {@code simulador.cors.origenes=https://mi-frontend.com,http://localhost:5173}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] origenes;

    public WebConfig(@Value("${simulador.cors.origenes:*}") String[] origenes) {
        this.origenes = origenes;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenes)
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}
