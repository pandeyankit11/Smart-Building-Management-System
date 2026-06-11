package com.smartbuilding.exception;

/**
 * Exception 1: InvalidAccessException
 * Thrown when a user attempts unauthorized access.
 */
public class InvalidAccessException extends Exception {
    private String userId;
    private String attemptedOperation;

    public InvalidAccessException(String message) {
        super(message);
    }

    public InvalidAccessException(String userId, String attemptedOperation) {
        super("User " + userId + " is not authorized to perform: " + attemptedOperation);
        this.userId = userId;
        this.attemptedOperation = attemptedOperation;
    }

    public InvalidAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getters
    public String getUserId() { return userId; }
    public String getAttemptedOperation() { return attemptedOperation; }
}
