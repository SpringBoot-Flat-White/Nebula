package com.nebula.nebulaCloud.exception;

/**
 * Exception thrown when an instance limit has been exceeded.
 * This typically results in a 409 Conflict response.
 */
public class InstanceLimitExceededException extends RuntimeException {
    
    public InstanceLimitExceededException(String message) {
        super(message);
    }
}

