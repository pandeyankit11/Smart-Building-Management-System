package com.smartbuilding.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LightingSystem class manages outdoor lighting.
 * Demonstrates nested non-static classes.
 */
public class LightingSystem extends BuildingComponent {
    private List<Light> lights;
    private LocalTime scheduleOn;
    private LocalTime scheduleOff;

    // Nested non-static class - Light
    public class Light {
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

        public void setBrightness(int level) throws Exception {
            if (level < 0 || level > 100) {
                throw new Exception("Brightness must be between 0 and 100");
            }
            this.brightnessLevel = level;
            System.out.println("Light " + lightId + " brightness set to " + level + "%");
        }

        public void adjustBrightness(int delta) throws Exception {
            int newLevel = brightnessLevel + delta;
            setBrightness(newLevel);
        }

        public void recordEnergyUsage(double usage) {
            this.energyUsed += usage;
        }

        // Getters
        public String getLightId() { return lightId; }
        public String getLocation() { return location; }
        public boolean isOn() { return isOn; }
        public int getBrightnessLevel() { return brightnessLevel; }
        public double getEnergyUsed() { return energyUsed; }

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
        this("LIT" + System.currentTimeMillis() % 10000, name, "Outdoor");
    }

    // Overloaded methods
    public void addLight(String lightId, String location) {
        Light light = new Light(lightId, location);
        lights.add(light);
        System.out.println("Light added: " + lightId + " at " + location);
    }

    public void addLight(String location) {
        String lightId = "LGT" + System.currentTimeMillis() % 10000;
        addLight(lightId, location);
    }

    public void removeLight(String lightId) {
        lights.removeIf(light -> light.lightId.equals(lightId));
        System.out.println("Light " + lightId + " removed from system");
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
        this.scheduleOn = onTime;
        this.scheduleOff = offTime;
        System.out.println("Lighting schedule set: ON at " + onTime + ", OFF at " + offTime);
    }

    public void autoControlBasedOnTime() {
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(scheduleOn) || currentTime.isBefore(scheduleOff)) {
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
