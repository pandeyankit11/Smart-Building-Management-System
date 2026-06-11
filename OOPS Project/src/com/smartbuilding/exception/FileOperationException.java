package com.smartbuilding.exception;

/**
 * Exception 4: FileOperationException
 * Thrown when file I/O operations fail.
 * Demonstrates I/O exception handling.
 */
public class FileOperationException extends Exception {
    private String fileName;
    private String operation;

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String fileName, String operation) {
        super("File operation failed: " + operation + " on file: " + fileName);
        this.fileName = fileName;
        this.operation = operation;
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getters
    public String getFileName() { return fileName; }
    public String getOperation() { return operation; }
}
