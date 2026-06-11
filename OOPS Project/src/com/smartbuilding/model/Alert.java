package com.smartbuilding.model;

import java.time.LocalDateTime;

/**
 * Alert class represents system alerts and notifications.
 * Part of the alert management system.
 */
public class Alert {
    private String alertId;
    private String alertType;
    private String message;
    private String severity; // INFO, WARNING, HIGH, CRITICAL
    private LocalDateTime timestamp;
    private String source;
    private boolean acknowledged;
    private String assignedTo;

    // Nested enum for alert types - demonstrates another nested type
    public enum AlertTypeEnum {
        EQUIPMENT_FAILURE,
        ENERGY_OVERUSE,
        SECURITY_BREACH,
        MAINTENANCE_REMINDER,
        OCCUPANCY_ALERT,
        SYSTEM_ERROR,
        LIGHT_MALFUNCTION
    }

    // Constructor
    public Alert(String alertId, String alertType, String message, String severity, String source) {
        this.alertId = alertId;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.acknowledged = false;
        this.assignedTo = null;
    }

    public Alert(String alertType, String message, String severity, String source) {
        this("ALT" + System.currentTimeMillis() % 10000, alertType, message, severity, source);
    }

    public void acknowledge(String assignedTo) {
        this.acknowledged = true;
        this.assignedTo = assignedTo;
        System.out.println("Alert " + alertId + " acknowledged by " + assignedTo);
    }

    public void updateSeverity(String newSeverity) {
        this.severity = newSeverity;
        System.out.println("Alert " + alertId + " severity updated to: " + newSeverity);
    }

    // Overloaded method for quick acknowledgment
    public void acknowledge() {
        this.acknowledged = true;
        this.assignedTo = "SYSTEM";
        System.out.println("Alert " + alertId + " acknowledged by system");
    }

    @Override
    public String toString() {
        return "Alert ID: " + alertId + ", Type: " + alertType + ", Severity: " + severity +
               ", Source: " + source + ", Time: " + timestamp + ", Acknowledged: " + acknowledged +
               (assignedTo != null ? ", Assigned to: " + assignedTo : "");
    }

    // Getters and setters
    public String getAlertId() { return alertId; }
    public String getAlertType() { return alertType; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSource() { return source; }
    public boolean isAcknowledged() { return acknowledged; }
    public String getAssignedTo() { return assignedTo; }

    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}
