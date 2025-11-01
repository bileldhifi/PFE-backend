package tn.esprit.exam.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application
 * Implements Spring Security best practices
 */
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Configure security filter chain
     * Uses constructor injection for dependencies
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) 
            throws Exception {
        http
                // Disable CSRF for REST API
                .csrf(csrf -> csrf.disable())
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register", 
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/users/add",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before username/password filter
                .addFilterBefore(
                        jwtAuthFilter, 
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
