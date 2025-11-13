package com.locallend.locallend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the LocalLend application.
 * Enables the frontend to communicate with the backend from different origins.
 */
@Configuration
public class CorsConfig {

    /**
     * CORS configuration source that defines allowed origins, methods, and headers.
     * This enables the React frontend (localhost:5173) to communicate with the backend (localhost:8080).
     * 
     * @return CorsConfigurationSource with appropriate CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from frontend origins
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost",          // Production (Nginx on port 80)
            "http://localhost:80",       // Production (explicit port 80)
            "http://localhost:3000",     // Create React App default
            "http://localhost:5173",     // Vite default port
            "http://127.0.0.1",          // Production (alternative localhost)
            "http://127.0.0.1:80",       // Production (alternative localhost port 80)
            "http://127.0.0.1:3000",     // Alternative localhost
            "http://127.0.0.1:5173"      // Alternative localhost
        ));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow all headers (including custom headers like Authorization)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply this configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}