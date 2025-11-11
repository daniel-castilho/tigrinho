package com.tigrinho.slot.service;

import com.tigrinho.slot.exception.InsufficientFundsException;
import com.tigrinho.slot.exception.ResourceNotFoundException;
import com.tigrinho.slot.model.entity.Player;
import com.tigrinho.slot.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for managing player wallets, implementing a "Hot Wallet"
 * strategy using Redis for fast, atomic operations and MongoDB as a "Cold Wallet"
 * for persistent storage.
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PlayerRepository playerRepository;

    private static final String BALANCE_KEY_PREFIX = "balance:";
    private static final long BALANCE_TTL_MINUTES = 60; // Time-to-live for balance in cache

    /**
     * Retrieves the player's balance from the "Hot Wallet" (Redis).
     * If the balance is not found in Redis (cache miss), it is loaded from MongoDB
     * and then cached in Redis.
     *
     * @param playerId The unique identifier of the player.
     * @return The player's current balance as a {@link BigDecimal}.
     * @throws ResourceNotFoundException if the player is not found when loading from MongoDB.
     */
    public BigDecimal getBalance(final String playerId) {
        final Long balanceInCents = getBalanceInCents(playerId);
        return centsToBigDecimal(balanceInCents);
    }

    /**
     * Debits a specified amount from the player's "Hot Wallet" (Redis) atomically.
     * Ensures the balance exists in Redis before debiting. If the debit results
     * in a negative balance, the operation is reverted, and an exception is thrown.
     *
     * @param playerId The unique identifier of the player.
     * @param amount The amount to debit.
     * @throws InsufficientFundsException if the player does not have enough funds.
     * @throws ResourceNotFoundException if the player is not found when loading from MongoDB.
     */
    public void debit(final String playerId, final BigDecimal amount) {
        final long amountInCents = bigDecimalToCents(amount);
        final String key = getPlayerBalanceKey(playerId);

        // Ensure balance exists in Redis before debiting
        getBalanceInCents(playerId);

        // Atomic DECRBY operation:
        final Long newBalance = redisTemplate.opsForValue().decrement(key, amountInCents);

        if (newBalance < 0) {
            // If it became negative, the debit failed. Revert the operation.
            redisTemplate.opsForValue().increment(key, amountInCents); // Revert
            throw new InsufficientFundsException(playerId);
        }

        // Update the key's time-to-live
        redisTemplate.expire(key, BALANCE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Credits a specified amount to the player's "Hot Wallet" (Redis) atomically.
     * Ensures the balance exists in Redis before crediting.
     *
     * @param playerId The unique identifier of the player.
     * @param amount The amount to credit.
     * @throws ResourceNotFoundException if the player is not found when loading from MongoDB.
     */
    public void credit(final String playerId, final BigDecimal amount) {
        final long amountInCents = bigDecimalToCents(amount);
        final String key = getPlayerBalanceKey(playerId);

        // Ensure balance exists in Redis before crediting
        getBalanceInCents(playerId);

        // Atomic INCRBY operation:
        redisTemplate.opsForValue().increment(key, amountInCents);

        // Update the key's time-to-live
        redisTemplate.expire(key, BALANCE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Retrieves the player's balance in cents from Redis.
     * If not found in Redis (cache miss), it fetches from MongoDB (Cold Wallet)
     * and saves it to Redis with a TTL.
     *
     * @param playerId The unique identifier of the player.
     * @return The player's balance in cents (Long).
     * @throws ResourceNotFoundException if the player is not found in MongoDB.
     */
    private Long getBalanceInCents(final String playerId) {
        final String key = getPlayerBalanceKey(playerId);
        final String balanceStr = redisTemplate.opsForValue().get(key);

        if (balanceStr == null) {
            // Cache miss: Fetch from MongoDB
            final Player player = findPlayerById(playerId);
            final Long balanceInCents = bigDecimalToCents(player.getBalance());

            // Save to Redis with TTL (Time-to-Live)
            redisTemplate.opsForValue().set(key, String.valueOf(balanceInCents), BALANCE_TTL_MINUTES, TimeUnit.MINUTES);
            return balanceInCents;
        }

        // Cache hit: Return the value from Redis
        return Long.parseLong(balanceStr);
    }

    /**
     * Constructs the Redis key for a player's balance.
     *
     * @param playerId The unique identifier of the player.
     * @return The Redis key string.
     */
    private String getPlayerBalanceKey(final String playerId) {
        return BALANCE_KEY_PREFIX + playerId;
    }

    /**
     * Finds a player by their ID from the repository.
     *
     * @param playerId The unique identifier of the player.
     * @return The {@link Player} entity.
     * @throws ResourceNotFoundException if the player is not found.
     */
    private Player findPlayerById(final String playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
    }

    /**
     * Converts a {@link BigDecimal} amount to cents (Long).
     *
     * @param amount The {@link BigDecimal} amount.
     * @return The amount in cents as a Long.
     */
    private long bigDecimalToCents(final BigDecimal amount) {
        // Multiplies by 100 and gets the Long value (e.g., 10.50 -> 1050)
        return amount.multiply(new BigDecimal("100")).longValue();
    }

    /**
     * Converts an amount in cents (Long) to a {@link BigDecimal}.
     *
     * @param cents The amount in cents.
     * @return The amount as a {@link BigDecimal}.
     */
    private BigDecimal centsToBigDecimal(final Long cents) {
        // Divides by 100 (e.g., 1050 -> 10.50)
        return new BigDecimal(cents).divide(new BigDecimal("100"));
    }
}
