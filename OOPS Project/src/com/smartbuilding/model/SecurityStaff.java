package com.smartbuilding.model;

/**
 * Security Staff class extends User.
 * Handles security monitoring, access logs, and incident reports.
 */
public class SecurityStaff extends User implements AlertListener {
    private String badgeNumber;
    private String shift;

    // Overloaded constructors
    public SecurityStaff(String userId, String username, String password, String badgeNumber, String shift) {
        super(userId, username, password, "SECURITY");
        this.badgeNumber = badgeNumber;
        this.shift = shift;
    }

    public SecurityStaff(String username, String password, String shift) {
        this("SEC" + System.currentTimeMillis() % 10000, username, password,
             "BADGE" + System.currentTimeMillis() % 10000, shift);
    }

    @Override
    public void receiveAlert(String alertType, String message, String severity) {
        if (alertType.equals("SECURITY_BREACH") || severity.equals("CRITICAL")) {
            System.out.println("[SECURITY ALERT - URGENT] " + message);
        } else {
            System.out.println("[SECURITY ALERT] " + message);
        }
    }

    @Override
    public boolean canHandleAlert(String alertType) {
        return alertType.equals("SECURITY_BREACH") || alertType.equals("UNAUTHORIZED_ACCESS");
    }

    @Override
    public void acknowledgeAlert(String alertId) {
        System.out.println("Security staff acknowledged security alert: " + alertId);
    }

    // Overloaded methods
    public void monitorAccessLogs() {
        System.out.println("Monitoring access logs in real-time...");
    }

    public void monitorAccessLogs(String date) {
        System.out.println("Monitoring access logs for date: " + date);
    }

    public void investigateIncident(String incidentId) {
        System.out.println("Investigating incident: " + incidentId);
    }

    public void investigateIncident(String incidentId, String priority) {
        System.out.println("Investigating incident " + incidentId + " with priority: " + priority);
    }

    public void generateSecurityReport() {
        System.out.println("Generating security report...");
    }

    // Getters
    public String getBadgeNumber() { return badgeNumber; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
}
