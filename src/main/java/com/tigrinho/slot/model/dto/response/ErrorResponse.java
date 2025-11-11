package com.tigrinho.slot.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response DTO for API errors.
 * This class provides a consistent structure for error messages returned by the API.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final String path;
    private final Map<String, Object> details;

    /**
     * Constructs a new ErrorResponse.
     *
     * @param timestamp The timestamp when the error occurred.
     * @param status The HTTP status code of the error.
     * @param message A brief message describing the error.
     * @param path The request URI that caused the error.
     * @param details A map containing additional details about the error, if any.
     */
    public ErrorResponse(final LocalDateTime timestamp, final int status, final String message, final String path, final Map<String, Object> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.details = details;
    }
}
