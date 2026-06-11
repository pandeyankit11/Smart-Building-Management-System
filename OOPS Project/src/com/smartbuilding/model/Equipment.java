package com.smartbuilding.model;

import java.time.LocalDate;

/**
 * Equipment class extends BuildingComponent.
 * Represents equipment/devices in the building.
 */
public class Equipment extends BuildingComponent {
    private String equipmentType;
    private String status; // OPERATIONAL, MAINTENANCE, MALFUNCTIONING
    private double energyConsumption;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    // Nested static class for equipment specifications
    public static class EquipmentSpecs {
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
        this.equipmentType = equipmentType;
        this.status = "OPERATIONAL";
        this.specs = specs;
        this.energyConsumption = energyConsumption;
        this.lastMaintenanceDate = LocalDate.now();
        this.nextMaintenanceDate = LocalDate.now().plusMonths(6);
    }

    public Equipment(String name, String location, String equipmentType, EquipmentSpecs specs) {
        this("EQP" + System.currentTimeMillis() % 10000, name, location, equipmentType, specs, 0.0);
    }

    public Equipment(String name, String location, String equipmentType, EquipmentSpecs specs, double energyConsumption) {
        this("EQP" + System.currentTimeMillis() % 10000, name, location, equipmentType, specs, energyConsumption);
    }

    public Equipment(String name, String equipmentType, double energyConsumption) {
        this(name, "Building", equipmentType, null, energyConsumption);
    }

    // Overloaded methods
    public void updateStatus(String newStatus) throws Exception {
        if (!newStatus.equals("OPERATIONAL") && !newStatus.equals("MAINTENANCE") && !newStatus.equals("MALFUNCTIONING")) {
            throw new Exception("Invalid status. Must be OPERATIONAL, MAINTENANCE, or MALFUNCTIONING");
        }
        this.status = newStatus;
        System.out.println("Equipment " + name + " status changed to: " + newStatus);
    }

    public void updateStatus(String newStatus, String reason) throws Exception {
        updateStatus(newStatus);
        System.out.println("Reason: " + reason);
    }

    public void recordEnergyUsage(double usage) {
        this.energyConsumption = usage;
        System.out.println("Energy consumption recorded for " + name + ": " + usage + " kWh");
    }

    public void scheduleMaintenance(LocalDate date) {
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

    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }
    public void setEnergyConsumption(double energyConsumption) { this.energyConsumption = energyConsumption; }
}
