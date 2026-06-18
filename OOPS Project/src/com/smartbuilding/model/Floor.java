package com.smartbuilding.model;

import java.util.ArrayList;
import java.util.List;
import com.smartbuilding.util.IdGenerator;

/**
 * Floor class extends BuildingComponent.
 * Represents a floor in the building with multiple rooms.
 */
public class Floor extends BuildingComponent {
    private static final long serialVersionUID = 1L;

    private int floorNumber;
    private List<Room> rooms;

    // Overloaded constructors
    public Floor(String componentId, String name, String location, int floorNumber) {
        super(componentId, name, location);
        if (floorNumber <= 0) {
            throw new IllegalArgumentException("Floor number must be greater than zero");
        }
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>();
    }

    public Floor(String name, int floorNumber) {
        this(IdGenerator.next("FLR"), name, "Building", floorNumber);
    }

    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        if (getRoomById(room.getComponentId()) != null) {
            throw new IllegalArgumentException("Room ID already exists: " + room.getComponentId());
        }
        rooms.add(room);
        System.out.println("Room " + room.getName() + " added to floor " + floorNumber);
    }

    public boolean removeRoom(String roomId) {
        boolean removed = rooms.removeIf(room -> room.getComponentId().equals(roomId));
        if (removed) {
            System.out.println("Room with ID " + roomId + " removed from floor " + floorNumber);
        }
        return removed;
    }

    public Room getRoomById(String roomId) {
        for (Room room : rooms) {
            if (room.getComponentId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    // Overloaded method - get rooms by equipment type
    public List<Room> getRoomsByEquipment(String equipmentType) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.hasEquipmentType(equipmentType)) {
                result.add(room);
            }
        }
        return result;
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Floor: " + name);
        for (Room room : rooms) {
            room.performMaintenance();
        }
    }

    // Getters
    public int getFloorNumber() { return floorNumber; }
    public List<Room> getRooms() { return new ArrayList<>(rooms); }
    public int getRoomCount() { return rooms.size(); }
}
