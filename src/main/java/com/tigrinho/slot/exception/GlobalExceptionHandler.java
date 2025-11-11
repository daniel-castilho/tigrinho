package com.tigrinho.slot.exception;

import com.tigrinho.slot.model.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the REST API.
 * This class intercepts exceptions thrown by controllers and services,
 * mapping them to appropriate HTTP responses with a standardized error format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException} and returns a 404 Not Found response.
     *
     * @param ex The {@link ResourceNotFoundException} that was thrown.
     * @param request The {@link HttpServletRequest} that caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            final ResourceNotFoundException ex, final HttpServletRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                "Resource not found");
    }

    /**
     * Handles {@link UsernameAlreadyExistsException} and returns a 409 Conflict response.
     *
     * @param ex The {@link UsernameAlreadyExistsException} that was thrown.
     * @param request The {@link HttpServletRequest} that caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 409.
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExistsException(
            final UsernameAlreadyExistsException ex, final HttpServletRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.CONFLICT, // 409 Conflict
                request.getRequestURI(),
                "Username already registered");
    }

    /**
     * Handles {@link InsufficientFundsException} and returns a 402 Payment Required response.
     *
     * @param ex The {@link InsufficientFundsException} that was thrown.
     * @param request The {@link HttpServletRequest} that caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 402.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(
            final InsufficientFundsException ex, final HttpServletRequest request) {

        return buildErrorResponse(
                ex,
                HttpStatus.PAYMENT_REQUIRED, // 402 Payment Required
                request.getRequestURI(),
                "Insufficient funds for this operation");
    }

    /**
     * Handles {@link MethodArgumentNotValidException} for validation errors
     * and returns a 400 Bad Request response.
     * It collects all field errors and includes them in the error response.
     *
     * @param ex The {@link MethodArgumentNotValidException} that was thrown.
     * @param request The {@link HttpServletRequest} that caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        // Collect errors more robustly
        final Map<String, Object> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Validation error",
                        (message1, message2) -> message1 + "; " + message2 // Merge messages for duplicate keys
                ));

        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getRequestURI(),
                errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link MethodArgumentTypeMismatchException} for type conversion errors
     * and returns a 400 Bad Request response.
     *
     * @param ex The {@link MethodArgumentTypeMismatchException} that was thrown.
     * @param request The {@link HttpServletRequest} that caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            final MethodArgumentTypeMismatchException ex, final HttpServletRequest request) {
        final String error = String.format("Parameter '%s' must be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        return buildErrorResponse(
                ex,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                error);
    }

    /**
     * Catches all other uncaught exceptions and returns a 500 Internal Server Error response.
     * This is a generic handler for unexpected errors.
     *
     * @param ex The {@link Exception} that was thrown.
     * @param request The {@link HttpServletRequest} that caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            final Exception ex, final HttpServletRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                "An unexpected error occurred");
    }

    /**
     * Helper method to construct a standardized {@link ErrorResponse}.
     *
     * @param ex The exception that occurred.
     * @param status The HTTP status to return.
     * @param path The request URI.
     * @param message A descriptive message for the error.
     * @return A {@link ResponseEntity} containing the constructed {@link ErrorResponse}.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            final Exception ex,
            final HttpStatus status,
            final String path,
            final String message) {

        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                message,
                path,
                new HashMap<>() {{
                    put("error", ex.getMessage());
                }});

        return new ResponseEntity<>(errorResponse, status);
    }
}
