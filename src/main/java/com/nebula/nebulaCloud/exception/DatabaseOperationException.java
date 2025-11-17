package com.nebula.nebulaCloud.exception;

/**
 * Exception thrown when a database operation fails.
 * This typically results in a 502 Bad Gateway response.
 */
public class DatabaseOperationException extends RuntimeException {
    
    public DatabaseOperationException(String message) {
        super(message);
    }
    
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

