package com.nebula.nebulaCloud.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for all API endpoints.
 * Catches and transforms exceptions into consistent JSON responses with appropriate HTTP status codes.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Build a standard error response
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request != null ? request.getRequestURI() : "N/A"
        );
        
        log.error("Error response: {} - {}", status, message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Handle ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handle DatabaseOperationException (502 Bad Gateway - external database error)
     */
    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseOperation(
            DatabaseOperationException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /**
     * Handle InstanceLimitExceededException (409 Conflict)
     */
    @ExceptionHandler(InstanceLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleInstanceLimitExceeded(
            InstanceLimitExceededException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handle DuplicateResourceException (409 Conflict)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handle InvalidStatusTransitionException (400 Bad Request)
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handle UnauthorizedAccessException (403 Forbidden)
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handle Spring Security AccessDeniedException (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage(), request);
    }

    /**
     * Handle SQL syntax errors (400 Bad Request)
     */
    @ExceptionHandler(SQLSyntaxErrorException.class)
    public ResponseEntity<ErrorResponse> handleSQLSyntaxError(
            SQLSyntaxErrorException ex,
            HttpServletRequest request) {
        String message = "SQL syntax error: " + sanitizeMessage(ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Handle general SQL exceptions (502 Bad Gateway)
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(
            SQLException ex,
            HttpServletRequest request) {
        String message = "Database error: " + sanitizeMessage(ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, message, request);
    }

    /**
     * Handle Spring Data access exceptions (502 Bad Gateway)
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request) {
        String message = "Data access error: " + sanitizeMessage(ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, message, request);
    }

    /**
     * Handle validation errors (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        String message = "Validation failed: " + errors;
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Handle IllegalArgumentException (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handle generic RuntimeException with smart detection
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        
        String message = ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred";
        String lowerMsg = message.toLowerCase();
        
        // Smart detection based on message content
        if (lowerMsg.contains("not found")) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, message, request);
        } else if (lowerMsg.contains("limit reached") || lowerMsg.contains("limit exceeded")) {
            return buildErrorResponse(HttpStatus.CONFLICT, message, request);
        } else if (lowerMsg.contains("already exists") || lowerMsg.contains("ya existe") || lowerMsg.contains("duplicate")) {
            return buildErrorResponse(HttpStatus.CONFLICT, message, request);
        } else if (lowerMsg.contains("forbidden") || lowerMsg.contains("does not belong") || 
                   lowerMsg.contains("not allowed") || lowerMsg.contains("unauthorized")) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, message, request);
        } else if (lowerMsg.contains("cannot connect") || lowerMsg.contains("cannot create") || 
                   lowerMsg.contains("error creating") || lowerMsg.contains("connection refused")) {
            return buildErrorResponse(HttpStatus.BAD_GATEWAY, message, request);
        } else if (lowerMsg.contains("invalid") || lowerMsg.contains("bad request")) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
        }
        
        // Default to Internal Server Error
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    /**
     * Handle UnsupportedOperationException (501 Not Implemented)
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(
            UnsupportedOperationException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_IMPLEMENTED, ex.getMessage(), request);
    }

    /**
     * Handle all other exceptions (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Unexpected error occurred", ex);
        String message = "An unexpected error occurred: " + sanitizeMessage(ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    /**
     * Sanitize error messages to avoid exposing sensitive information
     */
    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "An error occurred";
        }
        
        // Truncate very long messages
        if (message.length() > 500) {
            return message.substring(0, 500) + "...";
        }
        
        return message;
    }
}

