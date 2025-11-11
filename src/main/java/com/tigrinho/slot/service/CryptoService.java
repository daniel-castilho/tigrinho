package com.tigrinho.slot.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for cryptographic operations used in the Provably Fair system.
 * This includes generating secure random seeds, hashing inputs with SHA-256,
 * and computing HMAC-SHA256.
 */
@Slf4j
@Service
public class CryptoService {

    private static final String ALGORITHM_HMAC = "HmacSHA256";
    private static final String ALGORITHM_HASH = "SHA-256";

    // SecureRandom is the preferred class for cryptographically secure random number generation.
    private SecureRandom secureRandom;

    /**
     * Initializes the {@link SecureRandom} instance after the bean has been constructed.
     */
    @PostConstruct
    public void init() {
        this.secureRandom = new SecureRandom();
        log.info("CryptoService initialized with SecureRandom.");
    }

    /**
     * Generates a secure random seed (32 bytes) and encodes it in Base64.
     * This is suitable for generating server seeds.
     *
     * @return A Base64 encoded string representing a secure random seed.
     */
    public String generateSeed() {
        final byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Computes the SHA-256 hash of a given input string.
     * Used to create the serverSeedHash for Provably Fair verification.
     *
     * @param input The string to be hashed.
     * @return The SHA-256 hash of the input as a hexadecimal string.
     * @throws RuntimeException if the SHA-256 algorithm is not found.
     */
    public String hash(final String input) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(ALGORITHM_HASH);
            final byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (final NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found: {}", ALGORITHM_HASH, e);
            throw new RuntimeException("Internal cryptography error", e);
        }
    }

    /**
     * Computes an HMAC-SHA256 for the given key and data.
     * This is the core function for generating deterministic game outcomes in the Provably Fair system.
     *
     * @param key The secret key (e.g., serverSeed).
     * @param data The data to be signed (e.g., clientSeed:nonce).
     * @return The resulting HMAC as a hexadecimal string, which is deterministic.
     * @throws RuntimeException if the HMAC algorithm is not found or the key is invalid.
     */
    public String hmac(final String key, final String data) {
        try {
            final Mac sha256_HMAC = Mac.getInstance(ALGORITHM_HMAC);
            final SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM_HMAC);
            sha256_HMAC.init(secret_key);

            final byte[] hmacBytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error calculating HMAC: {}", e.getMessage(), e);
            throw new RuntimeException("Internal cryptography error", e);
        }
    }

    /**
     * Helper method to convert a byte array into a hexadecimal string representation.
     *
     * @param hash The byte array to convert.
     * @return A hexadecimal string.
     */
    private String bytesToHex(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
