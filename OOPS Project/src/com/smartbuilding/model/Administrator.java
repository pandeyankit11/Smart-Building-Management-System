package com.smartbuilding.model;

/**
 * Administrator class extends User - demonstrates hierarchical inheritance.
 * Administrators have full system control.
 */
public class Administrator extends User implements AlertListener {
    private String adminLevel;

    // Overloaded constructors
    public Administrator(String userId, String username, String password, String adminLevel) {
        super(userId, username, password, "ADMINISTRATOR");
        this.adminLevel = adminLevel;
    }

    public Administrator(String username, String password) {
        this("ADM" + System.currentTimeMillis() % 10000, username, password, "SUPER");
    }

    @Override
    public void receiveAlert(String alertType, String message, String severity) {
        System.out.println("[ADMIN ALERT] Type: " + alertType + ", Message: " + message + ", Severity: " + severity);
        System.out.println("Administrator is taking necessary actions...");
    }

    @Override
    public boolean canHandleAlert(String alertType) {
        return true; // Admin can handle all alerts
    }

    @Override
    public void acknowledgeAlert(String alertId) {
        System.out.println("Administrator acknowledged alert: " + alertId);
    }

    public void manageSystem() {
        System.out.println("Administrator is managing the entire system.");
    }

    public void addUser(User user) {
        System.out.println("Administrator added user: " + user.getUsername());
    }

    public void removeUser(String userId) {
        System.out.println("Administrator removed user with ID: " + userId);
    }

    // Getters and setters
    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
}
