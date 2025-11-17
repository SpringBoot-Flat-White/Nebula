package com.nebula.nebulaCloud.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't own.
 * This typically results in a 403 Forbidden response.
 */
public class UnauthorizedAccessException extends RuntimeException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}

