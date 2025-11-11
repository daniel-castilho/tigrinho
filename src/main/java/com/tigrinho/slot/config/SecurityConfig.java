package com.tigrinho.slot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Tigrinho Slot Game application.
 * This class defines security filters, password encoders, and authorization rules.
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides a {@link PasswordEncoder} bean for encoding and verifying passwords.
     * Uses BCrypt hashing algorithm.
     *
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for HTTP requests.
     * Currently, security is largely disabled to facilitate API development.
     * CSRF protection is disabled, and all requests are permitted.
     * This configuration should be updated with proper JWT or other authentication
     * mechanisms before deployment to production.
     *
     * @param http The {@link HttpSecurity} object to configure.
     * @return A configured {@link SecurityFilterChain} instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permits all requests
                );
        return http.build();
    }
}
