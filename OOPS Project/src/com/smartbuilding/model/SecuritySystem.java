package com.smartbuilding.model;

import com.smartbuilding.util.IdGenerator;
import com.smartbuilding.exception.InvalidInputException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SecuritySystem class manages security operations.
 * Includes access logs, surveillance, alarm systems, and incident reports.
 */
public class SecuritySystem extends BuildingComponent {
    private static final long serialVersionUID = 1L;

    private List<AccessLog> accessLogs;
    private List<Incident> incidents;
    private List<Alarm> alarms;
    private boolean surveillanceActive;

    // Nested static class - AccessLog
    public static class AccessLog implements Serializable {
        private static final long serialVersionUID = 1L;

        private String logId;
        private String userId;
        private String location;
        private LocalDateTime accessTime;
        private String accessType; // ENTRY or EXIT
        private boolean authorized;

        public AccessLog(String logId, String userId, String location, LocalDateTime accessTime, String accessType, boolean authorized) {
            this.logId = logId;
            this.userId = userId;
            this.location = location;
            this.accessTime = accessTime;
            this.accessType = accessType;
            this.authorized = authorized;
        }

        @Override
        public String toString() {
            return "Log ID: " + logId + ", User: " + userId + ", Location: " + location +
                   ", Time: " + accessTime + ", Type: " + accessType + ", Authorized: " + authorized;
        }

        // Getters
        public String getLogId() { return logId; }
        public String getUserId() { return userId; }
        public String getLocation() { return location; }
        public LocalDateTime getAccessTime() { return accessTime; }
        public String getAccessType() { return accessType; }
        public boolean isAuthorized() { return authorized; }
    }

    // Nested static class - Incident
    public static class Incident implements Serializable {
        private static final long serialVersionUID = 1L;

        private String incidentId;
        private String type;
        private String description;
        private LocalDateTime reportedTime;
        private String status; // OPEN, INVESTIGATING, RESOLVED
        private String severity;

        public Incident(String incidentId, String type, String description, LocalDateTime reportedTime, String severity) {
            this.incidentId = incidentId;
            this.type = type;
            this.description = description;
            this.reportedTime = reportedTime;
            this.status = "OPEN";
            this.severity = severity;
        }

        public void updateStatus(String newStatus) {
            String normalized = newStatus == null ? "" : newStatus.trim().toUpperCase();
            if (!normalized.equals("OPEN") && !normalized.equals("INVESTIGATING")
                    && !normalized.equals("RESOLVED")) {
                throw new IllegalArgumentException("Invalid incident status: " + newStatus);
            }
            this.status = normalized;
            System.out.println("Incident " + incidentId + " status updated to: " + normalized);
        }

        @Override
        public String toString() {
            return "Incident ID: " + incidentId + ", Type: " + type + ", Severity: " + severity +
                   ", Status: " + status + ", Reported: " + reportedTime + ", Description: " + description;
        }

        // Getters
        public String getIncidentId() { return incidentId; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getReportedTime() { return reportedTime; }
        public String getStatus() { return status; }
        public String getSeverity() { return severity; }
    }

    // Nested static class - Alarm
    public static class Alarm implements Serializable {
        private static final long serialVersionUID = 1L;

        private String alarmId;
        private String alarmType;
        private LocalDateTime triggeredTime;
        private boolean isActive;
        private String location;

        public Alarm(String alarmId, String alarmType, String location) {
            this.alarmId = alarmId;
            this.alarmType = alarmType;
            this.location = location;
            this.triggeredTime = LocalDateTime.now();
            this.isActive = true;
        }

        public void deactivate() {
            this.isActive = false;
            System.out.println("Alarm " + alarmId + " deactivated");
        }

        @Override
        public String toString() {
            return "Alarm ID: " + alarmId + ", Type: " + alarmType + ", Location: " + location +
                   ", Active: " + isActive + ", Triggered: " + triggeredTime;
        }

        // Getters
        public String getAlarmId() { return alarmId; }
        public String getAlarmType() { return alarmType; }
        public LocalDateTime getTriggeredTime() { return triggeredTime; }
        public boolean isActive() { return isActive; }
        public String getLocation() { return location; }
    }

    // Constructor
    public SecuritySystem(String componentId, String name, String location) {
        super(componentId, name, location);
        this.accessLogs = new ArrayList<>();
        this.incidents = new ArrayList<>();
        this.alarms = new ArrayList<>();
        this.surveillanceActive = true;
    }

    public SecuritySystem(String name) {
        this(IdGenerator.next("SEC"), name, "Building");
    }

    // Overloaded methods
    public void logAccess(String userId, String location, String accessType, boolean authorized) {
        String normalizedType = accessType == null ? "" : accessType.trim().toUpperCase();
        if (!normalizedType.equals("ENTRY") && !normalizedType.equals("EXIT")) {
            throw new IllegalArgumentException("Access type must be ENTRY or EXIT");
        }
        AccessLog log = new AccessLog(
            IdGenerator.next("LOG"),
            requireText(userId, "User ID"), requireText(location, "Location"),
            LocalDateTime.now(), normalizedType, authorized
        );
        accessLogs.add(log);
        System.out.println("Access logged: " + userId + " " + normalizedType + " at " + location);
    }

    public void logAccess(String userId, String location) {
        logAccess(userId, location, "ENTRY", true);
    }

    public void reportIncident(String type, String description, String severity) {
        Incident incident = new Incident(
            IdGenerator.next("INC"),
            requireText(type, "Incident type"), requireText(description, "Description"),
            LocalDateTime.now(), normalizeSeverity(severity)
        );
        incidents.add(incident);
        System.out.println("Incident reported: " + type + " - " + description);
    }

    public void triggerAlarm(String alarmType, String location) {
        Alarm alarm = new Alarm(
            IdGenerator.next("ALM"),
            requireText(alarmType, "Alarm type"), requireText(location, "Location")
        );
        alarms.add(alarm);
        System.out.println("Alarm triggered: " + alarmType + " at " + location);
    }

    public void investigateIncident(String incidentId) throws InvalidInputException {
        for (Incident incident : incidents) {
            if (incident.getIncidentId().equals(incidentId)) {
                incident.updateStatus("INVESTIGATING");
                System.out.println("Now investigating incident: " + incidentId);
                return;
            }
        }
        throw new InvalidInputException("incidentId", incidentId, "existing incident ID");
    }

    public void resolveIncident(String incidentId) throws InvalidInputException {
        for (Incident incident : incidents) {
            if (incident.getIncidentId().equals(incidentId)) {
                incident.updateStatus("RESOLVED");
                System.out.println("Incident " + incidentId + " resolved");
                return;
            }
        }
        throw new InvalidInputException("incidentId", incidentId, "existing incident ID");
    }

    public Incident getIncidentById(String incidentId) {
        for (Incident incident : incidents) {
            if (incident.getIncidentId().equals(incidentId)) {
                return incident;
            }
        }
        return null;
    }

    public Alarm getAlarmById(String alarmId) {
        for (Alarm alarm : alarms) {
            if (alarm.getAlarmId().equals(alarmId)) {
                return alarm;
            }
        }
        return null;
    }

    public boolean deleteAccessLog(String logId) {
        return accessLogs.removeIf(log -> log.getLogId().equals(logId));
    }

    public boolean deleteIncident(String incidentId) {
        return incidents.removeIf(incident -> incident.getIncidentId().equals(incidentId));
    }

    public boolean deactivateAlarm(String alarmId) {
        Alarm alarm = getAlarmById(alarmId);
        if (alarm == null) {
            return false;
        }
        alarm.deactivate();
        return true;
    }

    public boolean deleteAlarm(String alarmId) {
        return alarms.removeIf(alarm -> alarm.getAlarmId().equals(alarmId));
    }

    // Vararg method - detect multiple unauthorized access attempts
    public void detectUnauthorizedAccesses(AccessLog... logs) {
        System.out.println("Checking for unauthorized accesses...");
        int unauthorizedCount = 0;
        for (AccessLog log : logs) {
            if (!log.isAuthorized()) {
                System.out.println("Unauthorized access detected: User " + log.getUserId() +
                                 " at " + log.getLocation() + " at " + log.getAccessTime());
                unauthorizedCount++;
            }
        }
        System.out.println("Total unauthorized attempts: " + unauthorizedCount);
    }

    // Overloaded method for getting reports
    public String generateSecurityReport(String period) {
        StringBuilder report = new StringBuilder();
        report.append("Security Report for ").append(period).append(":\n");
        report.append("Total Access Logs: ").append(accessLogs.size()).append("\n");
        report.append("Total Incidents: ").append(incidents.size()).append("\n");
        report.append("Active Alarms: ");
        long activeAlarms = alarms.stream().filter(Alarm::isActive).count();
        report.append(activeAlarms).append("\n");
        report.append("Surveillance Status: ").append(surveillanceActive ? "Active" : "Inactive");
        return report.toString();
    }

    public String generateSecurityReport() {
        return generateSecurityReport("All Time");
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Security System: " + name);
        surveillanceActive = false;
        System.out.println("Surveillance temporarily disabled for maintenance");
        // Reset after maintenance simulation
        surveillanceActive = true;
    }

    // Getters
    public List<AccessLog> getAccessLogs() { return new ArrayList<>(accessLogs); }
    public List<Incident> getIncidents() { return new ArrayList<>(incidents); }
    public List<Alarm> getAlarms() { return new ArrayList<>(alarms); }
    public boolean isSurveillanceActive() { return surveillanceActive; }

    public void setSurveillanceActive(boolean surveillanceActive) {
        this.surveillanceActive = surveillanceActive;
    }

    private static String normalizeSeverity(String severity) {
        String normalized = severity == null ? "" : severity.trim().toUpperCase();
        if (!normalized.equals("INFO") && !normalized.equals("WARNING")
                && !normalized.equals("HIGH") && !normalized.equals("CRITICAL")) {
            throw new IllegalArgumentException("Invalid severity: " + severity);
        }
        return normalized;
    }
}
