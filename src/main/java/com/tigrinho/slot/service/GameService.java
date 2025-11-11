package com.tigrinho.slot.service;

import com.tigrinho.slot.model.dto.event.WalletSyncEvent;
import com.tigrinho.slot.model.dto.response.SpinResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service responsible for orchestrating the game logic for a single spin.
 * This includes debiting bets, generating spin results using Provably Fair mechanics,
 * crediting winnings, and dispatching wallet synchronization events.
 */
@Service
@RequiredArgsConstructor
public class GameService {

    private final WalletService walletService;
    private final RNGService rngService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Executes the logic for a complete game spin.
     *
     * @param playerId The ID of the player performing the spin.
     * @param betAmount The amount of money the player is betting.
     * @return A {@link SpinResponse} containing the spin results, winnings, and new balance.
     */
    public SpinResponse performSpin(final String playerId, final BigDecimal betAmount) {

        // 1. Debit the bet amount from the player's hot wallet (Redis)
        walletService.debit(playerId, betAmount);

        // 2. Generate the game result using Provably Fair mechanics
        final RNGService.SpinResult spinResult = rngService.generateSpinResult(playerId, betAmount);

        // 3. Credit winnings to the player's hot wallet (Redis), if any
        if (spinResult.winAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletService.credit(playerId, spinResult.winAmount());
        }

        // 4. Fetch the final balance from the hot wallet (Redis)
        final BigDecimal newBalance = walletService.getBalance(playerId);

        // 5. Dispatch a wallet synchronization event to RabbitMQ (fire-and-forget)
        final WalletSyncEvent event = new WalletSyncEvent(playerId, newBalance);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);

        // 6. Return the response to the player
        return new SpinResponse(
                spinResult.symbols(),
                spinResult.winAmount(),
                newBalance
        );
    }
}
