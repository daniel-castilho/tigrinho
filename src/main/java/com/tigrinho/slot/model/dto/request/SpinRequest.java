package com.tigrinho.slot.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Represents a request to perform a game spin.
 * This record includes validation constraints for the bet amount.
 *
 * @param betAmount The amount of money the player is betting on the spin.
 *                  Must not be null and must be at least 0.01.
 */
public record SpinRequest(
        @NotNull(message = "Bet amount cannot be null")
        @DecimalMin(value = "0.01", message = "Bet amount must be at least 0.01")
        BigDecimal betAmount
) {
}
