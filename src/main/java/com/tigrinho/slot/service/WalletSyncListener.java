package com.tigrinho.slot.service;

import com.tigrinho.slot.model.dto.event.WalletSyncEvent;
import com.tigrinho.slot.model.entity.Player;
import com.tigrinho.slot.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Service that listens for {@link WalletSyncEvent} messages from RabbitMQ
 * and updates the player's balance in the persistent storage (MongoDB).
 * This ensures eventual consistency between the hot wallet (Redis) and cold wallet (MongoDB).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletSyncListener {

    private final PlayerRepository playerRepository;

    /**
     * Listens for messages on the configured RabbitMQ queue and processes {@link WalletSyncEvent}s.
     * When an event is received, it finds the player by ID and updates their balance in MongoDB.
     *
     * @param event The {@link WalletSyncEvent} containing the player ID and new balance.
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onWalletSync(final WalletSyncEvent event) {
        log.debug("Received wallet sync event: {}", event);

        playerRepository.findById(event.playerId()).ifPresent(player -> {
            player.setBalance(event.newBalance());
            playerRepository.save(player);
            log.info("Player {} balance updated to {}", player.getId(), player.getBalance());
        });
    }
}
