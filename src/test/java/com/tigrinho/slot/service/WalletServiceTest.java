package com.tigrinho.slot.service;

import com.tigrinho.slot.exception.InsufficientFundsException;
import com.tigrinho.slot.exception.ResourceNotFoundException;
import com.tigrinho.slot.model.entity.Player;
import com.tigrinho.slot.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link WalletService}.
 * This class verifies the hot wallet logic using Redis, including
 * balance retrieval, debit, credit, and cache miss/hit scenarios.
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WalletService walletService;

    private final String playerId = "player1";
    private final String playerBalanceKey = "balance:player1";

    /**
     * Sets up the test environment before each test.
     * Configures the mock {@link RedisTemplate} to return the mock {@link ValueOperations}.
     */
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // --- Tests for getBalance ---

    /**
     * Tests that {@link WalletService#getBalance(String)} returns the balance
     * directly from Redis when there is a cache hit.
     */
    @Test
    @DisplayName("getBalance | Should return balance from Redis (Cache Hit)")
    void getBalance_shouldReturnBalanceFromRedis_whenCacheHit() {
        // Given
        when(valueOperations.get(playerBalanceKey)).thenReturn("15050"); // R$ 150.50 in cents

        // When
        final BigDecimal balance = walletService.getBalance(playerId);

        // Then
        assertThat(balance).isEqualByComparingTo("150.50");
        verify(playerRepository, never()).findById(any()); // Verify DB was not accessed
    }

    /**
     * Tests that {@link WalletService#getBalance(String)} fetches the balance
     * from the database and caches it in Redis when there is a cache miss.
     */
    @Test
    @DisplayName("getBalance | Should return balance from DB and cache it in Redis (Cache Miss)")
    void getBalance_shouldReturnBalanceFromDbAndCacheIt_whenCacheMiss() {
        // Given
        final Player player = Player.builder().balance(new BigDecimal("200.00")).build();
        when(valueOperations.get(playerBalanceKey)).thenReturn(null); // Cache miss
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // When
        final BigDecimal balance = walletService.getBalance(playerId);

        // Then
        assertThat(balance).isEqualByComparingTo("200.00");
        verify(playerRepository).findById(playerId); // Verify DB was accessed
        // Verify balance was saved to Redis with the correct TTL
        verify(valueOperations).set(playerBalanceKey, "20000", 60, TimeUnit.MINUTES);
    }

    /**
     * Tests that {@link WalletService#getBalance(String)} throws a
     * {@link ResourceNotFoundException} if the player is not found during a cache miss.
     */
    @Test
    @DisplayName("getBalance | Should throw exception if player not found on cache miss")
    void getBalance_shouldThrowException_whenPlayerNotFoundOnCacheMiss() {
        // Given
        when(valueOperations.get(playerBalanceKey)).thenReturn(null); // Cache miss
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> walletService.getBalance(playerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Tests for debit ---

    /**
     * Tests that {@link WalletService#debit(String, BigDecimal)} successfully
     * debits the amount from Redis.
     */
    @Test
    @DisplayName("debit | Should debit successfully using Redis")
    void debit_shouldSucceed() {
        // Given
        // Simulate a cache hit to avoid DB access
        when(valueOperations.get(playerBalanceKey)).thenReturn("10000"); // Initial balance of 100.00

        final long amountInCents = 1000L; // R$ 10.00
        when(valueOperations.decrement(playerBalanceKey, amountInCents)).thenReturn(9000L); // Remaining balance positive

        // When
        walletService.debit(playerId, new BigDecimal("10.00"));

        // Then
        verify(valueOperations).decrement(playerBalanceKey, amountInCents);
        verify(redisTemplate).expire(playerBalanceKey, 60, TimeUnit.MINUTES); // Verify TTL was renewed
        verify(valueOperations, never()).increment(anyString(), anyLong()); // Ensure no reversion was called
    }

    /**
     * Tests that {@link WalletService#debit(String, BigDecimal)} throws an
     * {@link InsufficientFundsException} and reverts the operation if funds are insufficient.
     */
    @Test
    @DisplayName("debit | Should throw exception and revert if insufficient funds")
    void debit_shouldFailAndRevert_whenInsufficientFunds() {
        // Given
        // Simulate a cache hit to avoid DB access
        when(valueOperations.get(playerBalanceKey)).thenReturn("5000"); // Initial balance of 50.00

        final long amountInCents = 10000L; // R$ 100.00
        // Atomic decrement returns the new value. If negative, balance was insufficient.
        when(valueOperations.decrement(playerBalanceKey, amountInCents)).thenReturn(-5000L);

        // When & Then
        assertThatThrownBy(() -> walletService.debit(playerId, new BigDecimal("100.00")))
                .isInstanceOf(InsufficientFundsException.class);

        // Verify the complete logic
        verify(valueOperations).decrement(playerBalanceKey, amountInCents);
        // Ensure the operation was reverted to maintain balance consistency
        verify(valueOperations).increment(playerBalanceKey, amountInCents);
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any()); // Should not renew TTL on failure
    }

    // --- Tests for credit ---

    /**
     * Tests that {@link WalletService#credit(String, BigDecimal)} successfully
     * credits the amount to Redis.
     */
    @Test
    @DisplayName("credit | Should credit successfully using Redis")
    void credit_shouldSucceed() {
        // Given
        // Simulate a cache hit to avoid DB access
        when(valueOperations.get(playerBalanceKey)).thenReturn("10000"); // Initial balance of 100.00

        final long amountInCents = 5000L; // R$ 50.00

        // When
        walletService.credit(playerId, new BigDecimal("50.00"));

        // Then
        verify(valueOperations).increment(playerBalanceKey, amountInCents);
        verify(redisTemplate).expire(playerBalanceKey, 60, TimeUnit.MINUTES);
    }
}
