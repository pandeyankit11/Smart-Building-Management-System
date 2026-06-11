package com.smartbuilding.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Building class aggregates all components of the building.
 * Main aggregation point for floors, systems, and monitors.
 */
public class Building extends BuildingComponent {
    private String buildingName;
    private String address;
    private List<Floor> floors;
    private LightingSystem lightingSystem;
    private SecuritySystem securitySystem;
    private OccupancyMonitor occupancyMonitor;

    // Overloaded constructors (2+ required)
    public Building(String componentId, String name, String location, String buildingName, String address) {
        super(componentId, name, location);
        this.buildingName = buildingName;
        this.address = address;
        this.floors = new ArrayList<>();
    }

    public Building(String buildingName, String address) {
        this("BLD" + System.currentTimeMillis() % 10000, "Main Building", "Primary Location",
             buildingName, address);
        // Initialize systems
        this.lightingSystem = new LightingSystem("Building Lighting");
        this.securitySystem = new SecuritySystem("Building Security");
        this.occupancyMonitor = new OccupancyMonitor("Building Occupancy Monitor");
    }

    public void addFloor(Floor floor) {
        floors.add(floor);
        System.out.println("Floor " + floor.getFloorNumber() + " added to building");
    }

    public void removeFloor(int floorNumber) {
        floors.removeIf(floor -> floor.getFloorNumber() == floorNumber);
        System.out.println("Floor " + floorNumber + " removed from building");
    }

    public Floor getFloorByNumber(int floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }

    // Overloaded method - get total equipment count
    public int getTotalEquipmentCount() {
        int total = 0;
        for (Floor floor : floors) {
            total += floor.getRoomCount();
        }
        return total;
    }

    // Overloaded with filter
    public int getTotalEquipmentCount(String equipmentType) {
        int total = 0;
        for (Floor floor : floors) {
            for (Room room : floor.getRooms()) {
                for (Equipment eq : room.getEquipmentList()) {
                    if (eq.getEquipmentType().equals(equipmentType)) {
                        total++;
                    }
                }
            }
        }
        return total;
    }

    public double getTotalEnergyConsumption() {
        double total = 0.0;
        for (Floor floor : floors) {
            for (Room room : floor.getRooms()) {
                for (Equipment eq : room.getEquipmentList()) {
                    total += eq.getEnergyConsumption();
                }
            }
        }
        if (lightingSystem != null) {
            total += lightingSystem.getTotalEnergyConsumption();
        }
        return total;
    }

    public int getTotalOccupancy() {
        int total = 0;
        for (Floor floor : floors) {
            for (Room room : floor.getRooms()) {
                total += room.getCurrentOccupancy();
            }
        }
        return total;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (Floor floor : floors) {
            for (Room room : floor.getRooms()) {
                total += room.getCapacity();
            }
        }
        return total;
    }

    @Override
    public void performMaintenance() {
        System.out.println("Building-wide maintenance initiated for: " + buildingName);
        for (Floor floor : floors) {
            floor.performMaintenance();
        }
        if (lightingSystem != null) lightingSystem.performMaintenance();
        if (securitySystem != null) securitySystem.performMaintenance();
        if (occupancyMonitor != null) occupancyMonitor.performMaintenance();
    }

    // Getters and setters
    public String getBuildingName() { return buildingName; }
    public String getAddress() { return address; }
    public List<Floor> getFloors() { return new ArrayList<>(floors); }
    public LightingSystem getLightingSystem() { return lightingSystem; }
    public SecuritySystem getSecuritySystem() { return securitySystem; }
    public OccupancyMonitor getOccupancyMonitor() { return occupancyMonitor; }

    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public void setAddress(String address) { this.address = address; }
    public void setLightingSystem(LightingSystem lightingSystem) { this.lightingSystem = lightingSystem; }
    public void setSecuritySystem(SecuritySystem securitySystem) { this.securitySystem = securitySystem; }
    public void setOccupancyMonitor(OccupancyMonitor occupancyMonitor) { this.occupancyMonitor = occupancyMonitor; }

    @Override
    public String toString() {
        return "Building: " + buildingName + ", Address: " + address +
               ", Floors: " + floors.size() + ", Total Occupancy: " + getTotalOccupancy() +
               "/" + getTotalCapacity();
    }
}
