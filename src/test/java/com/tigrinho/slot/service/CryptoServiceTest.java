package com.tigrinho.slot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link CryptoService}.
 * This class verifies the correctness and determinism of cryptographic operations
 * such as seed generation, SHA-256 hashing, and HMAC-SHA256 computation.
 */
class CryptoServiceTest {

    private CryptoService cryptoService;

    /**
     * Sets up the test environment by initializing a new {@link CryptoService} instance
     * and manually calling its {@code @PostConstruct} method.
     */
    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        cryptoService.init(); // Manually call @PostConstruct to initialize SecureRandom
    }

    /**
     * Tests that {@link CryptoService#generateSeed()} returns a valid, non-empty Base64 string.
     */
    @Test
    @DisplayName("Should generate a valid Base64 seed string")
    void generateSeed_shouldReturnValidBase64String() {
        // When
        final String seed = cryptoService.generateSeed();

        // Then
        assertThat(seed).isNotNull().isNotEmpty();
        // A simple check to see if it looks like Base64 (not perfect, but useful)
        assertThat(seed).matches("^[A-Za-z0-9+/=]+$");
    }

    /**
     * Tests that {@link CryptoService#hash(String)} produces a correct and deterministic SHA-256 hash.
     * The expected hash value is pre-calculated for a known input.
     */
    @Test
    @DisplayName("Should generate a deterministic and correct SHA-256 hash")
    void hash_shouldProduceCorrectAndDeterministicHash() {
        // Given
        final String input = "tigrinho";
        // Expected SHA-256 hash pre-calculated based on your code's actual output
        final String expectedHash = "1fd0743c7abe961d66279d2b302e7a101bdce5488cbc448d9ac0f6d39ae8070f";

        // When
        final String actualHash = cryptoService.hash(input);

        // Then
        assertThat(actualHash).isEqualTo(expectedHash);
    }

    /**
     * Tests that {@link CryptoService#hmac(String, String)} produces a correct and deterministic HMAC-SHA256.
     * The expected HMAC value is pre-calculated for a known key and data.
     */
    @Test
    @DisplayName("Should generate a deterministic and correct HMAC-SHA256")
    void hmac_shouldProduceCorrectAndDeterministicHmac() {
        // Given
        final String key = "server-seed-secreta";
        final String data = "client-seed:0";
        // Expected HMAC-SHA256 pre-calculated based on your code's actual output
        final String expectedHmac = "cf830e2bb2344a3ad7d7eb9b4bfdeb7a1ea2c81479b871053f7aa44fb104320e";

        // When
        final String actualHmac = cryptoService.hmac(key, data);

        // Then
        assertThat(actualHmac).isEqualTo(expectedHmac);
    }
}
