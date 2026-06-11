package com.smartbuilding.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Room class extends BuildingComponent.
 * Represents a room/zone in the building.
 */
public class Room extends BuildingComponent {
    private int capacity;
    private int currentOccupancy;
    private List<Equipment> equipmentList;

    // Overloaded constructors (2+ required)
    public Room(String componentId, String name, String location, int capacity) {
        super(componentId, name, location);
        this.capacity = capacity;
        this.currentOccupancy = 0;
        this.equipmentList = new ArrayList<>();
    }

    public Room(String name, String location, int capacity) {
        this("RM" + System.currentTimeMillis() % 10000, name, location, capacity);
    }

    public Room(String name, int capacity) {
        this(name, "Building", capacity);
    }

    // Convenience constructor for quick room creation with floor number
    public Room(String name, int floorNumber, int capacity) {
        this(name, "Floor " + floorNumber, capacity);
    }

    public void addEquipment(Equipment equipment) {
        equipmentList.add(equipment);
        System.out.println("Equipment " + equipment.getName() + " added to room " + name);
    }

    public void removeEquipment(String equipmentId) {
        equipmentList.removeIf(eq -> eq.getComponentId().equals(equipmentId));
        System.out.println("Equipment with ID " + equipmentId + " removed from room " + name);
    }

    public Equipment getEquipmentById(String equipmentId) {
        for (Equipment eq : equipmentList) {
            if (eq.getComponentId().equals(equipmentId)) {
                return eq;
            }
        }
        return null;
    }

    // Overloaded method - get equipment by status
    public List<Equipment> getEquipmentByStatus(String status) {
        List<Equipment> result = new ArrayList<>();
        for (Equipment eq : equipmentList) {
            if (eq.getStatus().equals(status)) {
                result.add(eq);
            }
        }
        return result;
    }

    public boolean hasEquipmentType(String type) {
        for (Equipment eq : equipmentList) {
            if (eq.getEquipmentType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public void updateOccupancy(int count) throws Exception {
        if (count < 0 || count > capacity) {
            throw new Exception("Invalid occupancy count. Must be between 0 and " + capacity);
        }
        this.currentOccupancy = count;
        System.out.println("Occupancy in room " + name + " updated to " + count);
    }

    public void incrementOccupancy() throws Exception {
        if (currentOccupancy >= capacity) {
            throw new Exception("Room " + name + " is at full capacity!");
        }
        currentOccupancy++;
        System.out.println("Occupancy in room " + name + " increased to " + currentOccupancy);
    }

    public void decrementOccupancy() throws Exception {
        if (currentOccupancy <= 0) {
            throw new Exception("Room " + name + " is already empty!");
        }
        currentOccupancy--;
        System.out.println("Occupancy in room " + name + " decreased to " + currentOccupancy);
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Room: " + name);
        for (Equipment eq : equipmentList) {
            eq.performMaintenance();
        }
    }

    // Getters and setters
    public int getCapacity() { return capacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public List<Equipment> getEquipmentList() { return new ArrayList<>(equipmentList); }
    public int getEquipmentCount() { return equipmentList.size(); }

    public double getOccupancyRate() {
        return capacity > 0 ? (double) currentOccupancy / capacity * 100 : 0;
    }
}
