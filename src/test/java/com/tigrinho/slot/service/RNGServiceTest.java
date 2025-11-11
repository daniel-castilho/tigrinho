package com.tigrinho.slot.service;

import com.tigrinho.slot.model.entity.Player;
import com.tigrinho.slot.repository.PlayerRepository;
import com.tigrinho.slot.service.strategy.WinStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link RNGService}.
 * This class verifies the deterministic generation of spin results,
 * the interaction with player data, cryptographic services, and win strategies.
 */
@ExtendWith(MockitoExtension.class)
class RNGServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private CryptoService cryptoService;
    @Mock
    private List<WinStrategy> winStrategies; // Mock of the list of strategies

    @InjectMocks
    private RNGService rngService;

    /**
     * Tests that {@link RNGService#generateSpinResult(String, BigDecimal)} produces
     * deterministic symbols, increments the player's nonce, and correctly calculates
     * the win amount based on mocked strategies.
     */
    @Test
    @DisplayName("Should generate a deterministic spin result and increment nonce")
    void generateSpinResult_shouldBeDeterministicAndIncrementNonce() {
        // Given
        final String playerId = "player1";
        final BigDecimal betAmount = new BigDecimal("10");

        // 1. Configure the player with known seeds and nonce
        final Player player = Player.builder()
                .id(playerId)
                .serverSeed("known-server-seed")
                .clientSeed("known-client-seed")
                .nonce(0L)
                .build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // 2. Configure CryptoService to return a known HMAC hash
        final String dataForHmac = "known-client-seed:0";
        // Pre-calculated hash for the test. Starts with '0' to force the first symbol.
        final String knownHmac = "0000000011111111222222223333333344444444555555556666666677777777";
        when(cryptoService.hmac("known-server-seed", dataForHmac)).thenReturn(knownHmac);

        // 3. Configure the mock list of strategies to find none (no win)
        // We use a mock of the interface to simulate the stream behavior
        @SuppressWarnings("unchecked")
        final WinStrategy mockStrategy = mock(WinStrategy.class);
        when(winStrategies.stream()).thenReturn(List.of(mockStrategy).stream());
        when(mockStrategy.matches(any())).thenReturn(false);

        // When
        final RNGService.SpinResult result = rngService.generateSpinResult(playerId, betAmount);

        // Then
        // 4. Verify that the generated symbols are as expected
        // The conversion logic uses modulo of the symbol list size (4).
        // Hash "00000000" -> BigInteger 0 -> 0 % 4 = 0 -> Symbol "CEREJA"
        // Hash "11111111" -> BigInteger 286331153 -> 286331153 % 4 = 1 -> Symbol "LARANJA"
        // Hash "22222222" -> BigInteger 572662306 -> 572662306 % 4 = 2 -> Symbol "SETE"
        assertThat(result.symbols()).containsExactly("CEREJA", "LARANJA", "SETE");
        assertThat(result.winAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        // 5. Verify that the nonce was incremented and the player was saved
        final ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        final Player savedPlayer = playerCaptor.getValue();
        assertThat(savedPlayer.getNonce()).isEqualTo(1L);

        // 6. Verify that cryptoService was called with the correct data
        verify(cryptoService).hmac("known-server-seed", dataForHmac);
    }
}
