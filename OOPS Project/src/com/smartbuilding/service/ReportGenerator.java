package com.smartbuilding.service;

import com.smartbuilding.model.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ReportGenerator generates various system reports.
 * Demonstrates wrapper class usage and overloading.
 */
public class ReportGenerator {
    private Building building;
    private SecuritySystem securitySystem;
    private OccupancyMonitor occupancyMonitor;
    private LightingSystem lightingSystem;

    // Constructor
    public ReportGenerator(Building building, SecuritySystem securitySystem,
                          OccupancyMonitor occupancyMonitor, LightingSystem lightingSystem) {
        this.building = building;
        this.securitySystem = securitySystem;
        this.occupancyMonitor = occupancyMonitor;
        this.lightingSystem = lightingSystem;
    }

    // Overloaded methods for generating different reports

    public String generateEnergyReport(String period) {
        StringBuilder report = new StringBuilder();
        report.append("=== ENERGY CONSUMPTION REPORT ===\n");
        report.append("Period: ").append(period).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        double totalEnergy = 0.0;
        report.append("Equipment Energy Usage:\n");

        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                for (Equipment eq : room.getEquipmentList()) {
                    double usage = eq.getEnergyConsumption();
                    totalEnergy += usage;
                    report.append("  - ").append(eq.getName()).append(": ")
                          .append(usage).append(" kWh\n");
                }
            }
        }

        // Lighting energy
        double lightingEnergy = lightingSystem.getTotalEnergyConsumption();
        totalEnergy += lightingEnergy;
        report.append("  - Lighting System: ").append(lightingEnergy).append(" kWh\n");

        report.append("\nTotal Energy Consumption: ").append(totalEnergy).append(" kWh\n");
        report.append("Average Daily Consumption: ").append(totalEnergy / 30).append(" kWh\n");

        return report.toString();
    }

    public String generateEnergyReport() {
        return generateEnergyReport("Monthly");
    }

    public String generateOccupancyReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        report.append("=== OCCUPANCY REPORT ===\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        Map<String, Integer> roomOccupancy = new HashMap<>();
        Map<String, Integer> roomCapacity = new HashMap<>();

        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                roomCapacity.put(room.getName(), room.getCapacity());
                roomOccupancy.put(room.getName(), room.getCurrentOccupancy());
            }
        }

        report.append("Room Occupancy Summary:\n");
        int totalOccupied = 0;
        int totalCapacity = 0;

        for (String roomName : roomOccupancy.keySet()) {
            int occupied = roomOccupancy.get(roomName);
            int capacity = roomCapacity.getOrDefault(roomName, 0);
            totalOccupied += occupied;
            totalCapacity += capacity;
            double percentage = capacity > 0 ? (double) occupied / capacity * 100 : 0;
            report.append("  ").append(roomName).append(": ")
                  .append(occupied).append("/").append(capacity)
                  .append(" (").append(String.format("%.1f", percentage)).append("%)\n");
        }

        double overallUtilization = totalCapacity > 0 ? (double) totalOccupied / totalCapacity * 100 : 0;
        report.append("\nTotal Occupancy: ").append(totalOccupied).append("/").append(totalCapacity);
        report.append(" (").append(String.format("%.1f", overallUtilization)).append("%)\n");

        return report.toString();
    }

    // Overloaded with date string parameters
    public String generateOccupancyReport(String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);
        return generateOccupancyReport(startDate, endDate);
    }

    // No-arg overloaded method - generate current period report
    public String generateOccupancyReport() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return generateOccupancyReport(startOfMonth, endOfMonth);
    }

    public String generateEquipmentReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== EQUIPMENT PERFORMANCE REPORT ===\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        Map<String, Integer> operationalCount = new HashMap<>();
        Map<String, Integer> maintenanceCount = new HashMap<>();
        Map<String, Integer> malfunctionCount = new HashMap<>();

        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                for (Equipment eq : room.getEquipmentList()) {
                    String type = eq.getEquipmentType();
                    operationalCount.put(type, operationalCount.getOrDefault(type, 0) + 1);
                    if (eq.getStatus().equals("MAINTENANCE")) {
                        maintenanceCount.put(type, maintenanceCount.getOrDefault(type, 0) + 1);
                    } else if (eq.getStatus().equals("MALFUNCTIONING")) {
                        malfunctionCount.put(type, malfunctionCount.getOrDefault(type, 0) + 1);
                    }
                }
            }
        }

        report.append("Equipment Status by Type:\n");
        Set<String> allTypes = new HashSet<>(operationalCount.keySet());
        allTypes.addAll(maintenanceCount.keySet());
        allTypes.addAll(malfunctionCount.keySet());

        for (String type : allTypes) {
            int operational = operationalCount.getOrDefault(type, 0);
            int maintenance = maintenanceCount.getOrDefault(type, 0);
            int malfunction = malfunctionCount.getOrDefault(type, 0);
            int total = operational + maintenance + malfunction;

            report.append("  ").append(type).append(": Total=").append(total)
                  .append(", Operational=").append(operational)
                  .append(", Maintenance=").append(maintenance)
                  .append(", Malfunctioning=").append(malfunction).append("\n");
        }

        return report.toString();
    }

    // Overloaded method with equipment type filter
    public String generateEquipmentReport(String equipmentType) {
        StringBuilder report = new StringBuilder();
        report.append("=== EQUIPMENT REPORT: ").append(equipmentType).append(" ===\n\n");
        int count = 1;

        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                for (Equipment eq : room.getEquipmentList()) {
                    if (eq.getEquipmentType().equals(equipmentType)) {
                        report.append(count++).append(". ").append(eq).append("\n");
                    }
                }
            }
        }

        report.append("\nTotal ").append(equipmentType).append(" equipment: ").append(count - 1).append("\n");
        return report.toString();
    }

    // Vararg method - generate comparison report for multiple equipment types
    public String generateEquipmentComparisonReport(String... equipmentTypes) {
        StringBuilder report = new StringBuilder();
        report.append("=== EQUIPMENT COMPARISON REPORT ===\n");

        Map<String, Integer> typeCounts = new HashMap<>();
        Map<String, Double> typeEnergy = new HashMap<>();

        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                for (Equipment eq : room.getEquipmentList()) {
                    String type = eq.getEquipmentType();
                    typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
                    typeEnergy.put(type, typeEnergy.getOrDefault(type, 0.0) + eq.getEnergyConsumption());
                }
            }
        }

        report.append("Equipment Type Analysis:\n");
        for (String type : equipmentTypes) {
            int count = typeCounts.getOrDefault(type, 0);
            double energy = typeEnergy.getOrDefault(type, 0.0);
            report.append("  ").append(type).append(": Count=").append(count)
                  .append(", Total Energy=").append(energy).append(" kWh");
            if (count > 0) {
                report.append(", Avg=").append(energy / count).append(" kWh");
            }
            report.append("\n");
        }

        return report.toString();
    }

    public String generateSecurityReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== SECURITY REPORT ===\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        report.append(securitySystem.generateSecurityReport()).append("\n\n");

        report.append("Recent Incidents:\n");
        List<SecuritySystem.Incident> incidents = securitySystem.getIncidents();
        for (int i = 0; i < Math.min(5, incidents.size()); i++) {
            report.append("  ").append(incidents.get(i)).append("\n");
        }

        report.append("\nActive Alarms:\n");
        List<SecuritySystem.Alarm> alarms = securitySystem.getAlarms();
        for (SecuritySystem.Alarm alarm : alarms) {
            if (alarm.isActive()) {
                report.append("  ").append(alarm).append("\n");
            }
        }

        return report.toString();
    }

    public String generateLightingReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== LIGHTING SYSTEM REPORT ===\n");
        report.append("Total Lights: ").append(lightingSystem.getLightCount()).append("\n\n");

        report.append("Light Details:\n");
        for (LightingSystem.Light light : lightingSystem.getLights()) {
            report.append("  ").append(light).append("\n");
        }

        report.append("\nTotal Energy Consumption: ").append(lightingSystem.getTotalEnergyConsumption()).append(" kWh\n");
        return report.toString();
    }

    // Overloaded method that accepts multiple parameters
    public String generateCombinedReport(String... reportTypes) {
        StringBuilder combinedReport = new StringBuilder();
        combinedReport.append("=== COMBINED SYSTEM REPORT ===\n\n");

        for (String type : reportTypes) {
            switch (type.toUpperCase()) {
                case "ENERGY":
                    combinedReport.append(generateEnergyReport()).append("\n");
                    break;
                case "OCCUPANCY":
                    combinedReport.append(generateOccupancyReport()).append("\n");
                    break;
                case "EQUIPMENT":
                    combinedReport.append(generateEquipmentReport()).append("\n");
                    break;
                case "SECURITY":
                    combinedReport.append(generateSecurityReport()).append("\n");
                    break;
                case "LIGHTING":
                    combinedReport.append(generateLightingReport()).append("\n");
                    break;
                default:
                    combinedReport.append("Unknown report type: ").append(type).append("\n");
            }
        }

        return combinedReport.toString();
    }

    // Setter methods
    public void setBuilding(Building building) { this.building = building; }
    public void setSecuritySystem(SecuritySystem securitySystem) { this.securitySystem = securitySystem; }
    public void setOccupancyMonitor(OccupancyMonitor occupancyMonitor) { this.occupancyMonitor = occupancyMonitor; }
    public void setLightingSystem(LightingSystem lightingSystem) { this.lightingSystem = lightingSystem; }
}
