package com.tigrinho.slot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for the main Spring Boot application context.
 * This test ensures that the application context loads successfully with the
 * specified test profile and properties.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
class TigrinhoApplicationIT extends TestConfig {

    /**
     * Verifies that the Spring application context loads successfully.
     * This test method does not perform any specific assertions beyond context loading.
     */
    @Test
    void contextLoads() {
        // This test will verify that the Spring application context loads successfully
    }
}
