package com.tigrinho.slot.model.dto.response;

/**
 * Represents the response after a player changes their Provably Fair seeds.
 * It includes the old server seed (for verification), the new server seed hash,
 * the new client seed, and the reset nonce.
 *
 * @param oldServerSeed The server seed that was active before the change (for player verification).
 * @param newServerSeedHash The hash of the newly generated server seed.
 * @param newClientSeed The new client seed provided by the player.
 * @param newNonce The nonce, which is typically reset to 0 after a seed change.
 */
public record ChangeSeedsResponse(
        String oldServerSeed,
        String newServerSeedHash,
        String newClientSeed,
        Long newNonce
) {
}
