package com.smartbuilding.model;

import com.smartbuilding.exception.InvalidInputException;
import com.smartbuilding.util.IdGenerator;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LightingSystem class manages outdoor lighting.
 * Demonstrates nested non-static classes.
 */
public class LightingSystem extends BuildingComponent {
    private static final long serialVersionUID = 1L;

    private List<Light> lights;
    private LocalTime scheduleOn;
    private LocalTime scheduleOff;

    // Nested non-static class - Light
    public class Light implements Serializable {
        private static final long serialVersionUID = 1L;

        private String lightId;
        private String location;
        private boolean isOn;
        private int brightnessLevel; // 0-100
        private double energyUsed;

        public Light(String lightId, String location) {
            this.lightId = lightId;
            this.location = location;
            this.isOn = false;
            this.brightnessLevel = 50; // default
            this.energyUsed = 0.0;
        }

        public void turnOn() {
            this.isOn = true;
            System.out.println("Light " + lightId + " at " + location + " turned ON");
        }

        public void turnOff() {
            this.isOn = false;
            System.out.println("Light " + lightId + " at " + location + " turned OFF");
        }

        public void setBrightness(int level) throws InvalidInputException {
            if (level < 0 || level > 100) {
                throw new InvalidInputException("brightness", String.valueOf(level), "0-100");
            }
            this.brightnessLevel = level;
            System.out.println("Light " + lightId + " brightness set to " + level + "%");
        }

        public void adjustBrightness(int delta) throws InvalidInputException {
            int newLevel = brightnessLevel + delta;
            setBrightness(newLevel);
        }

        public void recordEnergyUsage(double usage) {
            if (usage < 0) {
                throw new IllegalArgumentException("Energy usage cannot be negative");
            }
            this.energyUsed += usage;
        }

        // Getters
        public String getLightId() { return lightId; }
        public String getLocation() { return location; }
        public boolean isOn() { return isOn; }
        public int getBrightnessLevel() { return brightnessLevel; }
        public double getEnergyUsed() { return energyUsed; }

        public void setLocation(String location) {
            this.location = requireText(location, "Light location");
        }

        @Override
        public String toString() {
            return "Light ID: " + lightId + ", Location: " + location + ", On: " + isOn +
                   ", Brightness: " + brightnessLevel + "%, Energy: " + energyUsed + " kWh";
        }
    }

    // Constructor
    public LightingSystem(String componentId, String name, String location) {
        super(componentId, name, location);
        this.lights = new ArrayList<>();
        this.scheduleOn = LocalTime.of(18, 0); // 6 PM default
        this.scheduleOff = LocalTime.of(6, 0); // 6 AM default
    }

    public LightingSystem(String name) {
        this(IdGenerator.next("LIT"), name, "Outdoor");
    }

    // Overloaded methods
    public void addLight(String lightId, String location) {
        if (getLightById(lightId) != null) {
            throw new IllegalArgumentException("Light ID already exists: " + lightId);
        }
        Light light = new Light(lightId, location);
        lights.add(light);
        System.out.println("Light added: " + lightId + " at " + location);
    }

    public void addLight(String location) {
        String lightId = IdGenerator.next("LGT");
        addLight(lightId, location);
    }

    public boolean removeLight(String lightId) {
        boolean removed = lights.removeIf(light -> light.lightId.equals(lightId));
        if (removed) {
            System.out.println("Light " + lightId + " removed from system");
        }
        return removed;
    }

    public Light getLightById(String lightId) {
        for (Light light : lights) {
            if (light.lightId.equals(lightId)) {
                return light;
            }
        }
        return null;
    }

    // Nested class demonstration - using outer class reference
    public void controlAllLights(boolean turnOn) {
        for (Light light : lights) {
            if (turnOn) {
                light.turnOn();
            } else {
                light.turnOff();
            }
        }
        System.out.println("All lights turned " + (turnOn ? "ON" : "OFF"));
    }

    public void setSchedule(LocalTime onTime, LocalTime offTime) {
        if (onTime == null || offTime == null || onTime.equals(offTime)) {
            throw new IllegalArgumentException("Lighting on/off times must be different and non-null");
        }
        this.scheduleOn = onTime;
        this.scheduleOff = offTime;
        System.out.println("Lighting schedule set: ON at " + onTime + ", OFF at " + offTime);
    }

    public void autoControlBasedOnTime() {
        LocalTime currentTime = LocalTime.now();
        boolean overnightSchedule = scheduleOn.isAfter(scheduleOff);
        boolean shouldBeOn = overnightSchedule
                ? !currentTime.isBefore(scheduleOn) || currentTime.isBefore(scheduleOff)
                : !currentTime.isBefore(scheduleOn) && currentTime.isBefore(scheduleOff);
        if (shouldBeOn) {
            controlAllLights(true);
        } else {
            controlAllLights(false);
        }
    }

    // Overloaded method for varargs - add multiple lights at once
    public void addMultipleLights(String... locations) {
        for (String location : locations) {
            addLight(location);
        }
    }

    public double getTotalEnergyConsumption() {
        double total = 0.0;
        for (Light light : lights) {
            total += light.energyUsed;
        }
        return total;
    }

    // Getter for lights
    public List<Light> getLights() {
        return new ArrayList<>(lights);
    }

    public int getLightCount() {
        return lights.size();
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Lighting System: " + name);
        for (Light light : lights) {
            System.out.println("Checking light: " + light.lightId);
        }
    }
}
