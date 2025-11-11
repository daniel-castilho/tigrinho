package com.tigrinho.slot.exception;

/**
 * Custom exception to indicate that a requested resource was not found.
 * This exception is typically mapped to an HTTP 404 Not Found status.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with a detailed message.
     *
     * @param resourceName The name of the resource that was not found (e.g., "Player").
     * @param fieldName The name of the field used to search for the resource (e.g., "id").
     * @param fieldValue The value of the field that was used (e.g., "player123").
     */
    public ResourceNotFoundException(final String resourceName, final String fieldName, final Object fieldValue) {
        super(String.format(
                "%s not found with %s : '%s'",
                resourceName,
                fieldName,
                fieldValue));
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message The detail message.
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
