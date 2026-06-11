package com.smartbuilding.exception;

/**
 * Exception 3: InvalidInputException
 * Thrown when user provides invalid input data.
 */
public class InvalidInputException extends Exception {
    private String fieldName;
    private String invalidValue;
    private String expectedFormat;

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String fieldName, String invalidValue, String expectedFormat) {
        super("Invalid input for field '" + fieldName + "': " + invalidValue +
              ". Expected format: " + expectedFormat);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.expectedFormat = expectedFormat;
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getters
    public String getFieldName() { return fieldName; }
    public String getInvalidValue() { return invalidValue; }
    public String getExpectedFormat() { return expectedFormat; }
}
