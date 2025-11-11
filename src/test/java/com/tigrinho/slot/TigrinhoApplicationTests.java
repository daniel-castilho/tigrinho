package com.tigrinho.slot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Basic Spring Boot test to ensure the application context loads successfully.
 * This test imports {@link TestContainersConfiguration} to provide necessary
 * infrastructure services for the application context.
 */
@Import(TestContainersConfiguration.class)
@SpringBootTest
class TigrinhoApplicationTests {

	/**
	 * Verifies that the Spring application context loads without errors.
	 */
	@Test
	void contextLoads() {
	}

}
