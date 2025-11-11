package com.tigrinho.slot.service.strategy;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for the Strategy Design Pattern.
 * Each implementation represents a specific win calculation rule for the slot game.
 */
public interface WinStrategy {

    /**
     * Checks if this win strategy is applicable to the given combination of symbols.
     *
     * @param symbols The list of symbols resulting from the spin.
     * @return {@code true} if the strategy applies, {@code false} otherwise.
     */
    boolean matches(final List<String> symbols);

    /**
     * Calculates the win amount based on the bet amount, according to this strategy's rule.
     *
     * @param betAmount The amount of money bet on the spin.
     * @return The calculated win amount.
     */
    BigDecimal calculateWin(final BigDecimal betAmount);
}
