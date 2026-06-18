package com.smartbuilding.model;

import com.smartbuilding.exception.InvalidInputException;
import com.smartbuilding.util.IdGenerator;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Equipment class extends BuildingComponent.
 * Represents equipment/devices in the building.
 */
public class Equipment extends BuildingComponent {
    private static final long serialVersionUID = 1L;

    private String equipmentType;
    private String status; // OPERATIONAL, MAINTENANCE, MALFUNCTIONING
    private double energyConsumption;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    // Nested static class for equipment specifications
    public static class EquipmentSpecs implements Serializable {
        private static final long serialVersionUID = 1L;

        private String modelNumber;
        private String manufacturer;
        private double powerRating;
        private String warrantyPeriod;

        public EquipmentSpecs(String modelNumber, String manufacturer, double powerRating, String warrantyPeriod) {
            this.modelNumber = modelNumber;
            this.manufacturer = manufacturer;
            this.powerRating = powerRating;
            this.warrantyPeriod = warrantyPeriod;
        }

        @Override
        public String toString() {
            return "Model: " + modelNumber + ", Manufacturer: " + manufacturer +
                   ", Power: " + powerRating + "W, Warranty: " + warrantyPeriod;
        }

        // Getters and setters
        public String getModelNumber() { return modelNumber; }
        public String getManufacturer() { return manufacturer; }
        public double getPowerRating() { return powerRating; }
        public String getWarrantyPeriod() { return warrantyPeriod; }
    }

    private EquipmentSpecs specs;

    // Overloaded constructors (2+ required)
    public Equipment(String componentId, String name, String location, String equipmentType,
                     EquipmentSpecs specs, double energyConsumption) {
        super(componentId, name, location);
        if (energyConsumption < 0) {
            throw new IllegalArgumentException("Energy consumption cannot be negative");
        }
        this.equipmentType = equipmentType;
        this.status = "OPERATIONAL";
        this.specs = specs;
        this.energyConsumption = energyConsumption;
        this.lastMaintenanceDate = LocalDate.now();
        this.nextMaintenanceDate = LocalDate.now().plusMonths(6);
    }

    public Equipment(String name, String location, String equipmentType, EquipmentSpecs specs) {
        this(IdGenerator.next("EQP"), name, location, equipmentType, specs, 0.0);
    }

    public Equipment(String name, String location, String equipmentType, EquipmentSpecs specs, double energyConsumption) {
        this(IdGenerator.next("EQP"), name, location, equipmentType, specs, energyConsumption);
    }

    public Equipment(String name, String equipmentType, double energyConsumption) {
        this(name, "Building", equipmentType, null, energyConsumption);
    }

    // Overloaded methods
    public void updateStatus(String newStatus) throws InvalidInputException {
        String normalizedStatus = newStatus == null ? "" : newStatus.trim().toUpperCase();
        if (!normalizedStatus.equals("OPERATIONAL") && !normalizedStatus.equals("MAINTENANCE")
                && !normalizedStatus.equals("MALFUNCTIONING")) {
            throw new InvalidInputException("status", String.valueOf(newStatus),
                    "OPERATIONAL, MAINTENANCE, or MALFUNCTIONING");
        }
        this.status = normalizedStatus;
        System.out.println("Equipment " + name + " status changed to: " + normalizedStatus);
    }

    public void updateStatus(String newStatus, String reason) throws InvalidInputException {
        updateStatus(newStatus);
        System.out.println("Reason: " + reason);
    }

    public void recordEnergyUsage(double usage) {
        if (usage < 0) {
            throw new IllegalArgumentException("Energy usage cannot be negative");
        }
        this.energyConsumption = usage;
        System.out.println("Energy consumption recorded for " + name + ": " + usage + " kWh");
    }

    public void scheduleMaintenance(LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Maintenance date cannot be null or in the past");
        }
        this.nextMaintenanceDate = date;
        System.out.println("Maintenance scheduled for " + name + " on " + date);
    }

    public void performMaintenance() {
        this.lastMaintenanceDate = LocalDate.now();
        this.nextMaintenanceDate = LocalDate.now().plusMonths(6);
        this.status = "OPERATIONAL";
        System.out.println("Maintenance completed for equipment: " + name);
    }

    // Vararg method - add multiple specifications (varargs required)
    public void addSpecifications(String... specs) {
        System.out.println("Adding specifications for " + name + ":");
        for (String spec : specs) {
            System.out.println("  - " + spec);
        }
    }

    // Getters and setters
    public String getEquipmentType() { return equipmentType; }
    public String getStatus() { return status; }
    public double getEnergyConsumption() { return energyConsumption; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public EquipmentSpecs getSpecs() { return specs; }

    public void updateDetails(String name, String location, String equipmentType,
                              double energyConsumption, String status) throws InvalidInputException {
        setName(name);
        setLocation(location);
        setEquipmentType(equipmentType);
        setEnergyConsumption(energyConsumption);
        updateStatus(status);
        System.out.println("Equipment details updated: " + this.name);
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = requireText(equipmentType, "Equipment type").toUpperCase();
    }
    public void setEnergyConsumption(double energyConsumption) {
        if (energyConsumption < 0) {
            throw new IllegalArgumentException("Energy consumption cannot be negative");
        }
        this.energyConsumption = energyConsumption;
    }
}
