package com.tigrinho.slot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class TigrinhoApplicationIT extends TestConfig {

    @Test
    void contextLoads() {
        // This test will verify that the Spring application context loads successfully
    }
}
