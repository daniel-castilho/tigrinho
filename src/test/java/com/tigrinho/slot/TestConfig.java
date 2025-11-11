package com.tigrinho.slot;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Abstract base class for integration tests in the Tigrinho Slot Game application.
 * This class provides common configurations for Spring Boot tests,
 * such as setting up a random web environment and activating the "test" profile.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class TestConfig {
    // Base test class with common configurations
}
