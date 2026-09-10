package com.tigrinho.slot.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to update a player's client seed for the Provably Fair system.
 * This record includes validation constraints for the client seed.
 *
 * @param clientSeed The new client seed provided by the player.
 *                   Must not be blank and between 1 and 100 characters.
 */
public record UpdateSeedsRequest(
        @NotBlank(message = "Client seed cannot be blank")
        @Size(min = UpdateSeedsRequest.MIN_CLIENT_SEED_LENGTH,
                max = UpdateSeedsRequest.MAX_CLIENT_SEED_LENGTH,
                message = "Client seed must be between 1 and 100 characters")
        String clientSeed
) {
    public static final int MIN_CLIENT_SEED_LENGTH = 1;
    public static final int MAX_CLIENT_SEED_LENGTH = 100;
}
