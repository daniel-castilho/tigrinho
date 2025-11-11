package com.tigrinho.slot.service;

import com.tigrinho.slot.exception.InsufficientFundsException;
import com.tigrinho.slot.model.dto.event.WalletSyncEvent;
import com.tigrinho.slot.model.dto.response.SpinResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link GameService}.
 * This class uses Mockito to isolate the service logic and verify interactions
 * with its dependencies ({@link WalletService}, {@link RNGService}, {@link RabbitTemplate}).
 */
@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private WalletService walletService;

    @Mock
    private RNGService rngService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private GameService gameService;

    /**
     * Sets up the test environment before each test.
     * It injects mock values for {@code @Value} annotated fields in {@link GameService}
     * that would normally be populated by Spring's application context.
     */
    @BeforeEach
    void setUp() {
        // Inject values that would be populated by @Value in Spring
        ReflectionTestUtils.setField(gameService, "exchangeName", "test-exchange");
        ReflectionTestUtils.setField(gameService, "routingKey", "test-key");
    }

    /**
     * Tests a successful spin scenario where the player wins.
     * Verifies that all service interactions occur in the correct order and with expected values.
     */
    @Test
    @DisplayName("Should perform a spin with a win successfully")
    void performSpin_WithWin() {
        // Given
        final String playerId = "player1";
        final BigDecimal betAmount = new BigDecimal("10.00");
        final BigDecimal winAmount = new BigDecimal("100.00");
        final BigDecimal finalBalance = new BigDecimal("190.00");

        final RNGService.SpinResult mockSpinResult = new RNGService.SpinResult(List.of("SETE", "SETE", "SETE"), winAmount);
        when(rngService.generateSpinResult(playerId, betAmount)).thenReturn(mockSpinResult);
        when(walletService.getBalance(playerId)).thenReturn(finalBalance);

        // When
        final SpinResponse response = gameService.performSpin(playerId, betAmount);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.winAmount()).isEqualByComparingTo(winAmount);
        assertThat(response.newBalance()).isEqualByComparingTo(finalBalance);

        final InOrder inOrder = inOrder(walletService, rngService);
        inOrder.verify(walletService).debit(playerId, betAmount);
        inOrder.verify(rngService).generateSpinResult(playerId, betAmount);
        inOrder.verify(walletService).credit(playerId, winAmount);
        inOrder.verify(walletService).getBalance(playerId);

        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test-key"), any(WalletSyncEvent.class));
    }

    /**
     * Tests a successful spin scenario where the player does not win.
     * Verifies that the credit operation is not called and other interactions are correct.
     */
    @Test
    @DisplayName("Should perform a spin with no win (without calling credit)")
    void performSpin_WithNoWin() {
        // Given
        final String playerId = "player1";
        final BigDecimal betAmount = new BigDecimal("10.00");
        final BigDecimal winAmount = BigDecimal.ZERO;
        final BigDecimal finalBalance = new BigDecimal("90.00");

        final RNGService.SpinResult mockSpinResult = new RNGService.SpinResult(List.of("CEREJA", "BAR", "SETE"), winAmount);
        when(rngService.generateSpinResult(playerId, betAmount)).thenReturn(mockSpinResult);
        when(walletService.getBalance(playerId)).thenReturn(finalBalance);

        // When
        final SpinResponse response = gameService.performSpin(playerId, betAmount);

        // Then
        assertThat(response.winAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.newBalance()).isEqualByComparingTo(finalBalance);

        final InOrder inOrder = inOrder(walletService, rngService);
        inOrder.verify(walletService).debit(playerId, betAmount);
        inOrder.verify(rngService).generateSpinResult(playerId, betAmount);
        inOrder.verify(walletService, never()).credit(any(), any()); // Ensure credit was NOT called
        inOrder.verify(walletService).getBalance(playerId);

        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test-key"), any(WalletSyncEvent.class));
    }

    /**
     * Tests that an {@link InsufficientFundsException} is propagated correctly
     * if the debit operation fails, and that no further processing occurs.
     */
    @Test
    @DisplayName("Should propagate exception and not continue if debit fails")
    void performSpin_ShouldPropagateException_WhenDebitFails() {
        // Given
        final String playerId = "player1";
        final BigDecimal betAmount = new BigDecimal("100.00");
        doThrow(new InsufficientFundsException(playerId)).when(walletService).debit(playerId, betAmount);

        // When & Then
        assertThatThrownBy(() -> gameService.performSpin(playerId, betAmount))
                .isInstanceOf(InsufficientFundsException.class);

        // Ensure the rest of the process did not continue
        verify(rngService, never()).generateSpinResult(anyString(), any());
        verify(walletService, never()).credit(anyString(), any());
        verify(walletService, never()).getBalance(anyString());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(WalletSyncEvent.class));
    }
}
