package com.tigrinho.slot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a player has insufficient funds for an operation.
 * This exception is typically mapped to an HTTP 402 Payment Required status.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED) // Maps to HTTP 402
public class InsufficientFundsException extends RuntimeException {
    /**
     * Constructs a new InsufficientFundsException with a message indicating
     * which player has insufficient funds.
     *
     * @param playerId The ID of the player with insufficient funds.
     */
    public InsufficientFundsException(final String playerId) {
        super("Insufficient funds for player: " + playerId);
    }
}
