package com.tigrinho.slot.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom health indicators in Spring Boot Actuator.
 * This class defines a custom HealthIndicator for the application.
 */
@Configuration
public class HealthCheckConfig {

    /**
     * Defines a custom HealthIndicator for the "Tigrinho Slot Service" application.
     * This indicator always reports the status as UP and provides additional details
     * about the service, its operational status, and version.
     *
     * @return An instance of {@link HealthIndicator} that reports the custom health.
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> Health.up()
                .withDetail("service", "tigrinho-slot-service")
                .withDetail("status", "operational")
                .withDetail("version", "1.0.0")
                .build();
    }
}
