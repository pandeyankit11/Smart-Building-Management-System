package com.smartbuilding.service;

import com.smartbuilding.model.*;
import com.smartbuilding.exception.InvalidInputException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * AlertSystem manages all alerts and notifications.
 * Demonstrates interface usage and comprehensive alert handling.
 */
public class AlertSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Alert> activeAlerts;
    private List<Alert> resolvedAlerts;
    private transient List<AlertListener> listeners;
    private Set<String> notifiedAlertIds;

    public AlertSystem() {
        this.activeAlerts = new ArrayList<>();
        this.resolvedAlerts = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.notifiedAlertIds = new HashSet<>();
    }

    // Overloaded method - create alert with different parameters
    public void createAlert(String alertType, String message, String severity) {
        Alert alert = new Alert(alertType, message, severity, "System");
        activeAlerts.add(alert);
        System.out.println("New alert created: " + alertType + " - " + message);
    }

    public void createAlert(String alertType, String message, String severity, String source) {
        Alert alert = new Alert(alertType, message, severity, source);
        activeAlerts.add(alert);
        System.out.println("New alert from " + source + ": " + alertType + " - " + message);
    }

    // Vararg method - create multiple alerts at once (varargs required)
    public void createAlerts(Alert... alerts) {
        for (Alert alert : alerts) {
            activeAlerts.add(alert);
            System.out.println("Alert added: " + alert.getAlertType());
        }
        System.out.println("Total " + alerts.length + " alerts added");
    }

    // Overloaded method for resolving alerts
    public void resolveAlert(String alertId) throws InvalidInputException {
        Alert alertToRemove = null;
        for (Alert alert : activeAlerts) {
            if (alert.getAlertId().equals(alertId)) {
                alertToRemove = alert;
                break;
            }
        }

        if (alertToRemove != null) {
            activeAlerts.remove(alertToRemove);
            resolvedAlerts.add(alertToRemove);
            System.out.println("Alert " + alertId + " resolved");
        } else {
            throw new InvalidInputException("alertId", alertId, "active alert ID");
        }
    }

    public void resolveAlert(Alert alert) {
        if (activeAlerts.remove(alert)) {
            resolvedAlerts.add(alert);
            System.out.println("Alert " + alert.getAlertId() + " resolved");
        }
    }

    public Alert getActiveAlertById(String alertId) {
        for (Alert alert : activeAlerts) {
            if (alert.getAlertId().equals(alertId)) {
                return alert;
            }
        }
        return null;
    }

    public boolean deleteAlert(String alertId) {
        boolean removed = activeAlerts.removeIf(alert -> alert.getAlertId().equals(alertId));
        removed = resolvedAlerts.removeIf(alert -> alert.getAlertId().equals(alertId)) || removed;
        notifiedAlertIds.remove(alertId);
        return removed;
    }

    public void notifyListeners() {
        System.out.println("Notifying " + listeners.size() + " listeners about " + activeAlerts.size() + " active alerts");
        for (AlertListener listener : listeners) {
            for (Alert alert : activeAlerts) {
                if (!notifiedAlertIds.contains(alert.getAlertId())
                        && listener.canHandleAlert(alert.getAlertType())) {
                    listener.receiveAlert(alert.getAlertType(), alert.getMessage(), alert.getSeverity());
                }
            }
        }
        for (Alert alert : activeAlerts) {
            notifiedAlertIds.add(alert.getAlertId());
        }
    }

    public void addListener(AlertListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            System.out.println("New listener added: " + listener.getClass().getSimpleName());
        }
    }

    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    // Overloaded method for sending equipment failure alert
    public void sendEquipmentFailureAlert(String equipmentName, String equipmentId, String failureDetails) {
        String message = "Equipment failure detected: " + equipmentName + " (ID: " + equipmentId + "). Details: " + failureDetails;
        createAlert("EQUIPMENT_FAILURE", message, "HIGH", "Equipment");
    }

    public void sendEquipmentFailureAlert(String equipmentName, String equipmentId) {
        sendEquipmentFailureAlert(equipmentName, equipmentId, "No additional details");
    }

    // Overloaded method for energy overuse alert
    public void sendEnergyOveruseAlert(String location, double currentUsage, double threshold) {
        String message = "Energy overuse at " + location + ". Current: " + currentUsage + " kWh, Threshold: " + threshold + " kWh";
        createAlert("ENERGY_OVERUSE", message, "WARNING", "EnergyMonitor");
    }

    // Method for security breach alert
    public void sendSecurityBreachAlert(String location, String breachDetails) {
        String message = "Security breach detected at " + location + ". Details: " + breachDetails;
        createAlert("SECURITY_BREACH", message, "CRITICAL", "Security");
    }

    // Overloaded method for maintenance reminder
    public void sendMaintenanceReminder(String equipmentName, LocalDate dueDate) {
        String message = "Maintenance due for " + equipmentName + ". Due date: " + dueDate;
        createAlert("MAINTENANCE_REMINDER", message, "INFO", "Scheduler");
    }

    // Overloaded method with varargs for batch alerts
    public void sendMaintenanceReminder(String equipmentName, LocalDate dueDate, String... additionalNotes) {
        StringBuilder notes = new StringBuilder();
        for (String note : additionalNotes) {
            notes.append(note).append("; ");
        }
        String message = "Maintenance due for " + equipmentName + ". Due date: " + dueDate + ". Notes: " + notes;
        createAlert("MAINTENANCE_REMINDER", message, "INFO", "Scheduler");
    }

    public void processAlerts() {
        System.out.println("\n=== Processing Active Alerts ===");
        for (int i = 0; i < activeAlerts.size(); i++) {
            Alert alert = activeAlerts.get(i);
            System.out.println((i + 1) + ". " + alert);
        }
        System.out.println("Total Active Alerts: " + activeAlerts.size());
    }

    public List<Alert> getActiveAlertsBySeverity(String severity) {
        List<Alert> result = new ArrayList<>();
        for (Alert alert : activeAlerts) {
            if (alert.getSeverity().equals(severity)) {
                result.add(alert);
            }
        }
        return result;
    }

    public int getActiveAlertCount() {
        return activeAlerts.size();
    }

    public int getResolvedAlertCount() {
        return resolvedAlerts.size();
    }

    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }

    public List<Alert> getResolvedAlerts() {
        return new ArrayList<>(resolvedAlerts);
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        listeners = new ArrayList<>();
    }
}
