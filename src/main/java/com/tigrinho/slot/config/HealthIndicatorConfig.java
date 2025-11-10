package com.tigrinho.slot.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthIndicatorConfig {

    @Bean
    public HealthIndicator customMongoHealthIndicator() {
        return () -> Health.up().withDetail("status", "MongoDB health check not implemented").build();
    }

    @Bean
    public HealthIndicator customRedisHealthIndicator() {
        return () -> Health.up().withDetail("status", "Redis health check not implemented").build();
    }
}
