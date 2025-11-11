package com.tigrinho.slot.model.dto.event;

import java.math.BigDecimal;

/**
 * Represents an event to synchronize a player's wallet balance.
 * This event is typically sent to a message queue (e.g., RabbitMQ)
 * to update the player's balance in the persistent storage (e.g., MongoDB).
 *
 * @param playerId The unique identifier of the player whose wallet needs synchronization.
 * @param newBalance The new balance of the player's wallet after an operation.
 */
public record WalletSyncEvent(
        String playerId,
        BigDecimal newBalance
) {
}
