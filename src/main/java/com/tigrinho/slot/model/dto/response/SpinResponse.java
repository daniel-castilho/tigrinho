package com.tigrinho.slot.model.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the response DTO for a single game spin.
 * It contains the visual outcome, the amount won, and the player's new balance.
 *
 * @param symbols The list of symbols resulting from the spin (e.g., ["CHERRY", "BAR", "SEVEN"]).
 * @param winAmount The amount won in this spin (can be 0.00 if no win).
 * @param newBalance The player's balance *after* this spin has been processed.
 */
public record SpinResponse(
        List<String> symbols,
        BigDecimal winAmount,
        BigDecimal newBalance
) {
    // Compact constructor for defensive copying
    public SpinResponse {
        // Defensive copy for the mutable list to prevent EI_EXPOSE_REP2
        // And make it unmodifiable to prevent EI_EXPOSE_REP when accessed
        symbols = Collections.unmodifiableList(new ArrayList<>(symbols));
    }
}
