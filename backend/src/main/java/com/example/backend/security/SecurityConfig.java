package com.example.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
/**
 * Cấu hình Security cho ứng dụng Spring Boot
 *
 * Mô tả:
 * - Bật Spring Security và method-level security (@EnableMethodSecurity)
 * - Sử dụng JWT (qua JwtAuthenticationFilter) cho authentication
 * - Whitelist một số public endpoints (auth, public, swagger, actuator)
 * - Stateless session (REST API) — không dùng session server-side
 */
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cấu hình filter chain:
        // - Disable CSRF (API không dùng form login)
        // - CORS: sử dụng cấu hình từ CorsConfig
        // - Whitelist các endpoint public (auth, swagger, actuator)
        // - Thêm JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
        // - Sử dụng stateless session (token-based)
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/error",
                                "/actuator/health",
                                // Swagger UI endpoints
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                // Uploaded files (avatars, etc.)
                                "/uploads/**")
                        .permitAll()
                        // Admin endpoints - includes SMS parser management
                        .requestMatchers("/api/admin/**", "/api/sms/parsers/**").hasRole("ADMIN")
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        // AuthenticationProvider dùng DaoAuthenticationProvider + UserDetailsService
        // - UserDetailsService sẽ load user (username/password/roles) từ DB
        // - BCryptPasswordEncoder dùng để compare password hash
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng BCrypt để hash password
        return new BCryptPasswordEncoder();
    }
}
