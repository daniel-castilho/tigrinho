package com.tigrinho.slot.model.dto.response;

/**
 * Represents the response DTO for Provably Fair data of a player.
 * This record provides the necessary information for a player to verify
 * the fairness of their game results.
 *
 * @param serverSeedHash The SHA-256 hash of the server seed, known before the game.
 * @param clientSeed The client seed provided by the player.
 * @param nonce The number of times the current seed pair has been used.
 */
public record ProvablyFairResponse(
        String serverSeedHash,
        String clientSeed,
        Long nonce
) {
}
