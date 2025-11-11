package com.tigrinho.slot.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to create a new player in the system.
 * This record includes validation constraints for username and password.
 *
 * @param username The desired username for the new player. Must not be blank and between 3 and 30 characters.
 * @param password The password for the new player. Must not be blank and between 6 and 100 characters.
 */
public record CreatePlayerRequest(
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
) {
}
