package com.nebula.nebulaCloud.exception;

/**
 * Exception thrown when an invalid status transition is attempted.
 * This typically results in a 400 Bad Request response.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}

