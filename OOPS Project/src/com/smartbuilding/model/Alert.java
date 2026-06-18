package com.smartbuilding.model;

import java.time.LocalDateTime;
import com.smartbuilding.util.IdGenerator;
import java.io.Serializable;

/**
 * Alert class represents system alerts and notifications.
 * Part of the alert management system.
 */
public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;

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
        this.severity = normalizeSeverity(severity);
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.acknowledged = false;
        this.assignedTo = null;
    }

    public Alert(String alertType, String message, String severity, String source) {
        this(IdGenerator.next("ALT"), alertType, message, normalizeSeverity(severity), source);
    }

    public void acknowledge(String assignedTo) {
        this.acknowledged = true;
        this.assignedTo = assignedTo;
        System.out.println("Alert " + alertId + " acknowledged by " + assignedTo);
    }

    public void updateSeverity(String newSeverity) {
        this.severity = normalizeSeverity(newSeverity);
        System.out.println("Alert " + alertId + " severity updated to: " + severity);
    }

    public void update(String message, String severity) {
        this.message = requireText(message, "Alert message");
        updateSeverity(severity);
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

    private static String normalizeSeverity(String severity) {
        String normalized = severity == null ? "" : severity.trim().toUpperCase();
        if (!normalized.equals("INFO") && !normalized.equals("WARNING")
                && !normalized.equals("HIGH") && !normalized.equals("CRITICAL")) {
            throw new IllegalArgumentException("Invalid alert severity: " + severity);
        }
        return normalized;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
