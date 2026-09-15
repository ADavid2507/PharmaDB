package pe.edu.upeu.PharmaBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:4200}") String[] allowedOrigins) {
        for (String origin : allowedOrigins) {
            if (origin.contains("*")) {
                throw new IllegalArgumentException("CORS requiere orígenes explícitos, sin comodines");
            }
        }
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "Authorization")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
