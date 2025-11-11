package com.tigrinho.slot.service;

import com.tigrinho.slot.TestContainersConfiguration;
import com.tigrinho.slot.model.dto.event.WalletSyncEvent;
import com.tigrinho.slot.model.entity.Player;
import com.tigrinho.slot.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test for the {@link WalletSyncListener}.
 * This test verifies the end-to-end flow of a wallet synchronization event:
 * sending a message to RabbitMQ and confirming the update in MongoDB.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
class WalletSyncListenerIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PlayerRepository playerRepository;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Sets up the test environment before each test.
     * Clears the player repository to ensure a clean state for each test.
     */
    @BeforeEach
    void setUp() {
        // Clear the database before each test
        playerRepository.deleteAll();
    }

    /**
     * Tests that the {@link WalletSyncListener} correctly consumes a
     * {@link WalletSyncEvent} and updates the player's balance in the database.
     * It uses Awaitility to handle the asynchronous nature of message processing.
     */
    @Test
    @DisplayName("Should consume sync event and update player balance in DB")
    void onWalletSync_shouldUpdatePlayerBalanceInDatabase() {
        // Given
        // 1. Create a player directly in the database with an initial balance
        final Player player = playerRepository.save(Player.builder()
                .username("sync_player")
                .balance(new BigDecimal("100.00"))
                .build());
        final String playerId = player.getId();
        final BigDecimal newBalance = new BigDecimal("500.00");

        // 2. Create the event to be sent to the queue
        final WalletSyncEvent event = new WalletSyncEvent(playerId, newBalance);

        // When
        // 3. Send the message to RabbitMQ
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);

        // Then
        // 4. Use Awaitility to asynchronously wait for the update in the database
        await()
                .atMost(5, TimeUnit.SECONDS) // Wait for a maximum of 5 seconds
                .pollInterval(100, TimeUnit.MILLISECONDS) // Check every 100ms
                .untilAsserted(() -> {
                    final Optional<Player> updatedPlayerOpt = playerRepository.findById(playerId);
                    assertThat(updatedPlayerOpt).isPresent();
                    assertThat(updatedPlayerOpt.get().getBalance()).isEqualByComparingTo(newBalance);
                });
    }
}
