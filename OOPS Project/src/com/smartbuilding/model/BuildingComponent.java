package com.smartbuilding.model;

/**
 * Abstract base class for all building components.
 * Demonstrates abstraction principle - cannot be instantiated directly.
 */
public abstract class BuildingComponent {
    protected String componentId;
    protected String name;
    protected String location;
    protected boolean isActive;

    public BuildingComponent(String componentId, String name, String location) {
        this.componentId = componentId;
        this.name = name;
        this.location = location;
        this.isActive = true;
    }

    // Abstract method - must be implemented by subclasses
    public abstract void performMaintenance();

    // Concrete method
    public void deactivate() {
        this.isActive = false;
        System.out.println(name + " has been deactivated.");
    }

    public void activate() {
        this.isActive = true;
        System.out.println(name + " has been activated.");
    }

    // Getters and setters
    public String getComponentId() { return componentId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return "Component ID: " + componentId + ", Name: " + name + ", Location: " + location + ", Active: " + isActive;
    }
}
