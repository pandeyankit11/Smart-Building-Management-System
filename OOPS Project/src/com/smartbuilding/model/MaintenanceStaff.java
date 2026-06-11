package com.smartbuilding.model;

/**
 * Maintenance Staff class extends User.
 * Maintenance staff can update equipment status and perform maintenance.
 */
public class MaintenanceStaff extends User implements AlertListener {
    private String staffId;
    private String specialization;

    // Overloaded constructors (2+ required)
    public MaintenanceStaff(String userId, String username, String password, String staffId, String specialization) {
        super(userId, username, password, "MAINTENANCE");
        this.staffId = staffId;
        this.specialization = specialization;
    }

    public MaintenanceStaff(String username, String password, String specialization) {
        this("STF" + System.currentTimeMillis() % 10000, username, password,
             "STF" + System.currentTimeMillis() % 10000, specialization);
    }

    @Override
    public void receiveAlert(String alertType, String message, String severity) {
        if (severity.equals("HIGH") || severity.equals("CRITICAL")) {
            System.out.println("[MAINTENANCE ALERT] Immediate attention needed: " + message);
        } else {
            System.out.println("[MAINTENANCE ALERT] " + message);
        }
    }

    @Override
    public boolean canHandleAlert(String alertType) {
        return alertType.equals("EQUIPMENT_FAILURE") || alertType.equals("MAINTENANCE_REMINDER");
    }

    @Override
    public void acknowledgeAlert(String alertId) {
        System.out.println("Maintenance staff acknowledged alert: " + alertId);
    }

    // Overloaded methods
    public void performMaintenance(String equipmentId) {
        System.out.println("Performing maintenance on equipment: " + equipmentId);
    }

    public void performMaintenance(String equipmentId, String description) {
        System.out.println("Performing maintenance on " + equipmentId + ". Description: " + description);
    }

    public void updateEquipmentStatus(String equipmentId, String status) {
        System.out.println("Equipment " + equipmentId + " status updated to: " + status);
    }

    public void scheduleMaintenance(String equipmentId, String date) {
        System.out.println("Maintenance scheduled for " + equipmentId + " on " + date);
    }

    // Getters
    public String getStaffId() { return staffId; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
