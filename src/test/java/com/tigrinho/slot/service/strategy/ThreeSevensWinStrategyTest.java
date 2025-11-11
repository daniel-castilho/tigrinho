package com.tigrinho.slot.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ThreeSevensWinStrategy}.
 * This class verifies the logic for matching three "SEVEN" symbols
 * and calculating the corresponding win amount.
 */
class ThreeSevensWinStrategyTest {

    private ThreeSevensWinStrategy strategy;

    /**
     * Sets up the test environment by initializing a new {@link ThreeSevensWinStrategy} instance.
     */
    @BeforeEach
    void setUp() {
        strategy = new ThreeSevensWinStrategy();
    }

    /**
     * Tests that {@link ThreeSevensWinStrategy#matches(List)} returns true
     * when the input symbols are exactly three "SEVEN"s.
     */
    @Test
    @DisplayName("Should return true when all 3 symbols are SETE")
    void matches_shouldReturnTrue_whenThreeSevens() {
        // Given
        final List<String> symbols = List.of("SETE", "SETE", "SETE");

        // When
        final boolean result = strategy.matches(symbols);

        // Then
        assertThat(result).isTrue();
    }

    /**
     * Tests that {@link ThreeSevensWinStrategy#matches(List)} returns false
     * for various invalid symbol combinations.
     *
     * @param symbols An invalid list of symbols.
     */
    @ParameterizedTest
    @MethodSource("invalidCombinations")
    @DisplayName("Should return false for invalid combinations")
    void matches_shouldReturnFalse_forInvalidCombinations(final List<String> symbols) {
        // When
        final boolean result = strategy.matches(symbols);

        // Then
        assertThat(result).isFalse();
    }

    /**
     * Tests that {@link ThreeSevensWinStrategy#calculateWin(BigDecimal)}
     * correctly applies the 100x multiplier to the bet amount.
     */
    @Test
    @DisplayName("Should calculate the win amount (100x)")
    void calculateWin_shouldReturn100x() {
        // Given
        final BigDecimal betAmount = new BigDecimal("10.00");

        // When
        final BigDecimal winAmount = strategy.calculateWin(betAmount);

        // Then
        assertThat(winAmount).isEqualByComparingTo("1000.00");
    }

    /**
     * Provides a stream of arguments for parameterized tests,
     * representing invalid symbol combinations for the {@link ThreeSevensWinStrategy}.
     *
     * @return A {@link Stream} of {@link Arguments} with invalid symbol lists.
     */
    private static Stream<Arguments> invalidCombinations() {
        return Stream.of(
                Arguments.of(List.of("SETE", "SETE", "CEREJA")),   // Wrong combination
                Arguments.of(List.of("CEREJA", "CEREJA", "CEREJA")), // 3 identical, but not SETE
                Arguments.of(List.of("BAR", "SETE", "LARANJA")),  // Different symbols
                Arguments.of(List.of("SETE", "SETE"))              // Wrong size
        );
    }
}
