package com.smartbuilding.model;

/**
 * Interface for components that can receive and process alerts.
 * Demonstrates interface implementation and polymorphism.
 */
public interface AlertListener {
    void receiveAlert(String alertType, String message, String severity);
    boolean canHandleAlert(String alertType);
    void acknowledgeAlert(String alertId);
}
