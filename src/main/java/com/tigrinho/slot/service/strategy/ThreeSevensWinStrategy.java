package com.tigrinho.slot.service.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implements a {@link WinStrategy} for the specific case where all three symbols are "SEVEN".
 * This strategy has a higher priority and awards a 100x multiplier.
 */
@Component
@Order(1) // Order of priority (lower number = higher priority)
public class ThreeSevensWinStrategy implements WinStrategy {

    private static final String SEVEN = "SETE";
    private static final BigDecimal MULTIPLIER = new BigDecimal("100");

    /**
     * Checks if the given list of symbols consists of three "SEVEN" symbols.
     *
     * @param symbols The list of symbols resulting from the spin.
     * @return {@code true} if all three symbols are "SEVEN", {@code false} otherwise.
     */
    @Override
    public boolean matches(final List<String> symbols) {
        // Checks if the list has 3 symbols and all are "SEVEN"
        return symbols.size() == 3 && symbols.stream().allMatch(SEVEN::equals);
    }

    /**
     * Calculates the win amount by multiplying the bet amount by 100.
     *
     * @param betAmount The amount of money bet on the spin.
     * @return The calculated win amount.
     */
    @Override
    public BigDecimal calculateWin(final BigDecimal betAmount) {
        return betAmount.multiply(MULTIPLIER);
    }
}
