package com.smartbuilding.model;

import java.io.Serializable;

/**
 * Interface for components that can receive and process alerts.
 * Demonstrates interface implementation and polymorphism.
 */
public interface AlertListener extends Serializable {
    void receiveAlert(String alertType, String message, String severity);
    boolean canHandleAlert(String alertType);
    void acknowledgeAlert(String alertId);
}
