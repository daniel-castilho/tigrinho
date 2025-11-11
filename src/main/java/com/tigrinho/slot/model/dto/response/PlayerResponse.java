package com.tigrinho.slot.model.dto.response;

import java.math.BigDecimal;

/**
 * Represents the response DTO for player information.
 * This record provides a summary of a player's public details.
 *
 * @param id The unique identifier of the player.
 * @param username The username of the player.
 * @param balance The current balance of the player's wallet.
 */
public record PlayerResponse(
        String id,
        String username,
        BigDecimal balance
) {
}
