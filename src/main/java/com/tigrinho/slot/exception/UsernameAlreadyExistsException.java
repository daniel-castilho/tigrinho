package com.tigrinho.slot.exception;

/**
 * Custom exception to indicate that a username already exists in the system.
 * This exception is typically mapped to an HTTP 409 Conflict status.
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new UsernameAlreadyExistsException with a message indicating
     * which username already exists.
     *
     * @param username The username that already exists.
     */
    public UsernameAlreadyExistsException(final String username) {
        super("Username already exists: " + username);
    }
}
