package com.smartbuilding.model;

import java.util.ArrayList;
import java.util.List;
import com.smartbuilding.exception.InvalidInputException;
import com.smartbuilding.util.IdGenerator;

/**
 * Room class extends BuildingComponent.
 * Represents a room/zone in the building.
 */
public class Room extends BuildingComponent {
    private static final long serialVersionUID = 1L;

    private int capacity;
    private int currentOccupancy;
    private List<Equipment> equipmentList;

    // Overloaded constructors (2+ required)
    public Room(String componentId, String name, String location, int capacity) {
        super(componentId, name, location);
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.currentOccupancy = 0;
        this.equipmentList = new ArrayList<>();
    }

    public Room(String name, String location, int capacity) {
        this(IdGenerator.next("RM"), name, location, capacity);
    }

    public Room(String name, int capacity) {
        this(name, "Building", capacity);
    }

    // Convenience constructor for quick room creation with floor number
    public Room(String name, int floorNumber, int capacity) {
        this(name, "Floor " + floorNumber, capacity);
    }

    public void addEquipment(Equipment equipment) {
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment cannot be null");
        }
        if (getEquipmentById(equipment.getComponentId()) != null) {
            throw new IllegalArgumentException("Equipment ID already exists: " + equipment.getComponentId());
        }
        equipmentList.add(equipment);
        System.out.println("Equipment " + equipment.getName() + " added to room " + name);
    }

    public boolean removeEquipment(String equipmentId) {
        boolean removed = equipmentList.removeIf(eq -> eq.getComponentId().equals(equipmentId));
        if (removed) {
            System.out.println("Equipment with ID " + equipmentId + " removed from room " + name);
        }
        return removed;
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
            if (eq.getStatus().equalsIgnoreCase(status)) {
                result.add(eq);
            }
        }
        return result;
    }

    public boolean hasEquipmentType(String type) {
        for (Equipment eq : equipmentList) {
            if (eq.getEquipmentType().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public void updateOccupancy(int count) throws InvalidInputException {
        if (count < 0 || count > capacity) {
            throw new InvalidInputException("occupancy", String.valueOf(count), "0-" + capacity);
        }
        this.currentOccupancy = count;
        System.out.println("Occupancy in room " + name + " updated to " + count);
    }

    public void incrementOccupancy() throws InvalidInputException {
        if (currentOccupancy >= capacity) {
            throw new InvalidInputException("Room " + name + " is at full capacity");
        }
        currentOccupancy++;
        System.out.println("Occupancy in room " + name + " increased to " + currentOccupancy);
    }

    public void decrementOccupancy() throws InvalidInputException {
        if (currentOccupancy <= 0) {
            throw new InvalidInputException("Room " + name + " is already empty");
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

    public void updateDetails(String name, String location, int capacity) {
        if (capacity <= 0 || capacity < currentOccupancy) {
            throw new IllegalArgumentException(
                    "Capacity must be positive and at least the current occupancy (" + currentOccupancy + ")");
        }
        setName(name);
        setLocation(location);
        this.capacity = capacity;
        System.out.println("Room details updated: " + this.name);
    }

    public double getOccupancyRate() {
        return capacity > 0 ? (double) currentOccupancy / capacity * 100 : 0;
    }
}
