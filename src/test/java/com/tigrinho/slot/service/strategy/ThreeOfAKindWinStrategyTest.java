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
 * Unit tests for the {@link ThreeOfAKindWinStrategy}.
 * This class verifies the logic for matching three identical symbols (excluding "SEVEN")
 * and calculating the corresponding win amount.
 */
class ThreeOfAKindWinStrategyTest {

    private ThreeOfAKindWinStrategy strategy;

    /**
     * Sets up the test environment by initializing a new {@link ThreeOfAKindWinStrategy} instance.
     */
    @BeforeEach
    void setUp() {
        strategy = new ThreeOfAKindWinStrategy();
    }

    /**
     * Tests that {@link ThreeOfAKindWinStrategy#matches(List)} returns true
     * for various combinations of three identical symbols (excluding "SEVEN").
     *
     * @param symbols A valid list of three identical symbols.
     */
    @ParameterizedTest
    @MethodSource("validCombinations")
    @DisplayName("Should return true for 3 identical symbols (excluding SETE)")
    void matches_shouldReturnTrue_forThreeOfAKind(final List<String> symbols) {
        // When
        final boolean result = strategy.matches(symbols);

        // Then
        assertThat(result).isTrue();
    }

    /**
     * Tests that {@link ThreeOfAKindWinStrategy#matches(List)} returns false
     * for various invalid symbol combinations, including three "SEVEN"s.
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
     * Tests that {@link ThreeOfAKindWinStrategy#calculateWin(BigDecimal)}
     * correctly applies the 10x multiplier to the bet amount.
     */
    @Test
    @DisplayName("Should calculate the win amount (10x)")
    void calculateWin_shouldReturn10x() {
        // Given
        final BigDecimal betAmount = new BigDecimal("10.00");

        // When
        final BigDecimal winAmount = strategy.calculateWin(betAmount);

        // Then
        assertThat(winAmount).isEqualByComparingTo("100.00");
    }

    /**
     * Provides a stream of arguments for parameterized tests,
     * representing valid symbol combinations for the {@link ThreeOfAKindWinStrategy}.
     *
     * @return A {@link Stream} of {@link Arguments} with valid symbol lists.
     */
    private static Stream<Arguments> validCombinations() {
        return Stream.of(
                Arguments.of(List.of("CEREJA", "CEREJA", "CEREJA")),
                Arguments.of(List.of("LARANJA", "LARANJA", "LARANJA")),
                Arguments.of(List.of("BAR", "BAR", "BAR"))
        );
    }

    /**
     * Provides a stream of arguments for parameterized tests,
     * representing invalid symbol combinations for the {@link ThreeOfAKindWinStrategy}.
     *
     * @return A {@link Stream} of {@link Arguments} with invalid symbol lists.
     */
    private static Stream<Arguments> invalidCombinations() {
        return Stream.of(
                Arguments.of(List.of("SETE", "SETE", "SETE")),     // Should be rejected by this strategy
                Arguments.of(List.of("CEREJA", "CEREJA", "BAR")), // Different symbols
                Arguments.of(List.of("CEREJA", "CEREJA"))         // Wrong size
        );
    }
}
