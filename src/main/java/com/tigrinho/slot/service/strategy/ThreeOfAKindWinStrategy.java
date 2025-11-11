package com.tigrinho.slot.service.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implements a {@link WinStrategy} for the case where all three symbols are identical,
 * but are not "SEVEN". This strategy has a lower priority than {@link ThreeSevensWinStrategy}
 * and awards a 10x multiplier.
 */
@Component
@Order(2) // Lower priority than ThreeSevensWinStrategy
public class ThreeOfAKindWinStrategy implements WinStrategy {

    private static final String SEVEN = "SETE";
    private static final BigDecimal MULTIPLIER = new BigDecimal("10");

    /**
     * Checks if the given list of symbols consists of three identical symbols,
     * none of which are "SEVEN".
     *
     * @param symbols The list of symbols resulting from the spin.
     * @return {@code true} if all three symbols are identical and not "SEVEN", {@code false} otherwise.
     */
    @Override
    public boolean matches(final List<String> symbols) {
        if (symbols.size() != 3) {
            return false;
        }
        // The first symbol cannot be "SEVEN"
        final String firstSymbol = symbols.get(0);
        if (firstSymbol.equals(SEVEN)) {
            return false;
        }
        // All symbols must be equal to the first one
        return symbols.stream().allMatch(firstSymbol::equals);
    }

    /**
     * Calculates the win amount by multiplying the bet amount by 10.
     *
     * @param betAmount The amount of money bet on the spin.
     * @return The calculated win amount.
     */
    @Override
    public BigDecimal calculateWin(final BigDecimal betAmount) {
        return betAmount.multiply(MULTIPLIER);
    }
}
