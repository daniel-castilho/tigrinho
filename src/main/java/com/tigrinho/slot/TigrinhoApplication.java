package com.tigrinho.slot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Tigrinho Slot Game Spring Boot application.
 * This class bootstraps and launches the application.
 */
@SpringBootApplication
public class TigrinhoApplication {

	/**
	 * The main method that starts the Spring Boot application.
	 *
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(final String[] args) {
		SpringApplication.run(TigrinhoApplication.class, args);
	}

}
