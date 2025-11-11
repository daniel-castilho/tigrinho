package com.tigrinho.slot.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom health indicators related to specific services
 * like MongoDB and Redis within the Spring Boot Actuator framework.
 * This class provides placeholder health checks for these components.
 */
@Configuration
public class HealthIndicatorConfig {

    /**
     * Defines a custom {@link HealthIndicator} for MongoDB.
     * Currently, this indicator reports a status of UP but notes that
     * the detailed MongoDB health check logic is not yet implemented.
     *
     * @return An instance of {@link HealthIndicator} for MongoDB.
     */
    @Bean
    public HealthIndicator customMongoHealthIndicator() {
        return () -> Health.up().withDetail("status", "MongoDB health check not implemented").build();
    }

    /**
     * Defines a custom {@link HealthIndicator} for Redis.
     * Currently, this indicator reports a status of UP but notes that
     * the detailed Redis health check logic is not yet implemented.
     *
     * @return An instance of {@link HealthIndicator} for Redis.
     */
    @Bean
    public HealthIndicator customRedisHealthIndicator() {
        return () -> Health.up().withDetail("status", "Redis health check not implemented").build();
    }
}
