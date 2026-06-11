package com.smartbuilding.model;

/**
 * General User class extends User.
 * General users can only view information, not modify anything.
 */
public class GeneralUser extends User implements AlertListener {
    private String apartmentNumber;

    // Overloaded constructors
    public GeneralUser(String userId, String username, String password, String apartmentNumber) {
        super(userId, username, password, "GENERAL_USER");
        this.apartmentNumber = apartmentNumber;
    }

    public GeneralUser(String username, String password, String apartmentNumber) {
        super("USR" + System.currentTimeMillis() % 10000, username, password, "GENERAL_USER");
        this.apartmentNumber = apartmentNumber;
    }

    public GeneralUser(String username, String apartmentNumber) {
        this("USR" + System.currentTimeMillis() % 10000, username, "default123", apartmentNumber);
    }

    @Override
    public void receiveAlert(String alertType, String message, String severity) {
        System.out.println("[USER NOTIFICATION] Alert: " + message + " (Severity: " + severity + ")");
    }

    @Override
    public boolean canHandleAlert(String alertType) {
        return false; // General users cannot handle alerts
    }

    @Override
    public void acknowledgeAlert(String alertId) {
        System.out.println("User acknowledged notification: " + alertId);
    }

    // Overloaded methods for viewing reports
    public void viewReport(String reportType) {
        System.out.println("Viewing " + reportType + " report...");
    }

    public void viewReport(String reportType, String period) {
        System.out.println("Viewing " + reportType + " report for period: " + period);
    }

    // Getters and setters
    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }
}
