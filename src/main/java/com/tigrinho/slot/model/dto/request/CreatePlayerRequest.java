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
        @Size(min = CreatePlayerRequest.MIN_USERNAME_LENGTH,
                max = CreatePlayerRequest.MAX_USERNAME_LENGTH,
                message = "Username must be between " + CreatePlayerRequest.MIN_USERNAME_LENGTH + " and "
                        + CreatePlayerRequest.MAX_USERNAME_LENGTH + " characters")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = CreatePlayerRequest.MIN_PASSWORD_LENGTH,
                max = CreatePlayerRequest.MAX_PASSWORD_LENGTH,
                message = "Password must be between " + CreatePlayerRequest.MIN_PASSWORD_LENGTH + " and "
                        + CreatePlayerRequest.MAX_PASSWORD_LENGTH + " characters")
        String password
) {
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 100;
}
