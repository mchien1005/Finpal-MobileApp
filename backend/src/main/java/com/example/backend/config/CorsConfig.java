package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
// import java.util.List;

/**
 * Cấu hình CORS cho phép frontend truy cập API
 * 
 * - Cho phép các origin từ localhost (development) và production domain
 * - Cho phép tất cả HTTP methods (GET, POST, PUT, DELETE, etc.)
 * - Cho phép các headers cần thiết (Authorization, Content-Type, etc.)
 */
@Configuration
public class CorsConfig {

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Allowed origins - thêm domain production nếu cần
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173", // Vite dev server
                                "http://localhost:3000", // React dev server
                                "http://localhost:8080", // Local backend
                                "http://127.0.0.1:5173",
                                "http://127.0.0.1:3000",
                                "http://175.41.150.228", // AWS production
                                "http://175.41.150.228:80",
                                "http://175.41.150.228:3000",
                                "https://finpal.vn", // Production domain (future)
                                "https://www.finpal.vn",
                                "https://systemsinc.systems", // New Production domain
                                "https://www.systemsinc.systems"));

                // Allowed HTTP methods
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

                // Allowed headers
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-Requested-With",
                                "Accept",
                                "Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers",
                                "Cache-Control"));

                // Exposed headers (headers that client can access)
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Disposition",
                                "X-Total-Count"));

                // Allow credentials (cookies, authorization headers)
                configuration.setAllowCredentials(true);

                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
