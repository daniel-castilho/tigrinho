package com.tigrinho.slot;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration class for setting up Testcontainers for integration tests.
 * This class defines beans for MongoDB, Redis, and RabbitMQ containers,
 * allowing Spring Boot to automatically configure service connections.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

	/**
	 * Provides a {@link MongoDBContainer} bean for testing purposes.
	 * The container is automatically managed by Testcontainers and connected
	 * to the Spring application context via {@link ServiceConnection}.
	 *
	 * @return A configured {@link MongoDBContainer}.
	 */
	@Bean
	@ServiceConnection
	MongoDBContainer mongoDbContainer() {
		return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
	}

	/**
	 * Provides a {@link GenericContainer} for Redis for testing purposes.
	 * The container exposes port 6379 and is automatically managed by Testcontainers
	 * and connected to the Spring application context via {@link ServiceConnection}.
	 *
	 * @return A configured {@link GenericContainer} for Redis.
	 */
	@Bean
	@ServiceConnection(name = "redis")
	GenericContainer<?> redisContainer() {
		return new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);
	}

	/**
	 * Provides a {@link RabbitMQContainer} bean for testing purposes.
	 * The container is automatically managed by Testcontainers and connected
	 * to the Spring application context via {@link ServiceConnection}.
	 * Spring will auto-configure 'spring.rabbitmq.*' properties.
	 *
	 * @return A configured {@link RabbitMQContainer}.
	 */
	@Bean
	@ServiceConnection // Spring will auto-configure 'spring.rabbitmq.*'
	RabbitMQContainer rabbitMqContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"));
	}
}
