package com.tigrinho.slot;

import org.springframework.boot.SpringApplication;

/**
 * Test-specific main class for the Tigrinho Slot Game application.
 * This class is used to run the application with test-specific configurations,
 * typically integrating with Testcontainers.
 */
public class TestTigrinhoApplication {

	/**
	 * The main method that starts the Spring Boot application for testing purposes.
	 * It configures the application to use {@link TestContainersConfiguration}.
	 *
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(final String[] args) {
		SpringApplication.from(TigrinhoApplication::main).with(TestContainersConfiguration.class).run(args);
	}

}
