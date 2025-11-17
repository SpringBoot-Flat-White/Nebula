package com.nebula.nebulaCloud.exception;

/**
 * Exception thrown when a resource already exists.
 * This typically results in a 409 Conflict response.
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
}

