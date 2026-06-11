package com.smartbuilding.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * OccupancyMonitor class tracks occupancy in rooms/zones.
 * Analyzes peak periods and optimizes utilities.
 */
public class OccupancyMonitor extends BuildingComponent {
    private Map<String, OccupancyRecord> occupancyRecords;
    private Map<String, Integer> roomCapacity;
    private LocalTime peakStartTime;
    private LocalTime peakEndTime;

    // Nested class - OccupancyRecord
    public class OccupancyRecord {
        private String roomId;
        private LocalDate date;
        private LocalTime time;
        private int occupantCount;
        private String zone;

        public OccupancyRecord(String roomId, LocalDate date, LocalTime time, int occupantCount, String zone) {
            this.roomId = roomId;
            this.date = date;
            this.time = time;
            this.occupantCount = occupantCount;
            this.zone = zone;
        }

        @Override
        public String toString() {
            return "Room: " + roomId + ", Date: " + date + ", Time: " + time +
                   ", Occupants: " + occupantCount + ", Zone: " + zone;
        }

        // Getters
        public String getRoomId() { return roomId; }
        public LocalDate getDate() { return date; }
        public LocalTime getTime() { return time; }
        public int getOccupantCount() { return occupantCount; }
        public String getZone() { return zone; }
    }

    // Constructor
    public OccupancyMonitor(String componentId, String name, String location) {
        super(componentId, name, location);
        this.occupancyRecords = new HashMap<>();
        this.roomCapacity = new HashMap<>();
        this.peakStartTime = LocalTime.of(9, 0);
        this.peakEndTime = LocalTime.of(17, 0);
    }

    public OccupancyMonitor(String name) {
        this("OCC" + System.currentTimeMillis() % 10000, name, "Building");
    }

    // Overloaded methods
    public void recordOccupancy(String roomId, int count) {
        OccupancyRecord record = new OccupancyRecord(
            roomId, LocalDate.now(), LocalTime.now(), count, "Default"
        );
        occupancyRecords.put(roomId + "_" + System.currentTimeMillis(), record);
        System.out.println("Occupancy recorded: Room " + roomId + " has " + count + " occupants");
    }

    public void recordOccupancy(String roomId, int count, String zone) {
        OccupancyRecord record = new OccupancyRecord(
            roomId, LocalDate.now(), LocalTime.now(), count, zone
        );
        occupancyRecords.put(roomId + "_" + System.currentTimeMillis(), record);
        System.out.println("Occupancy recorded: Room " + roomId + " (Zone: " + zone + ") has " + count + " occupants");
    }

    public void setRoomCapacity(String roomId, int capacity) {
        roomCapacity.put(roomId, capacity);
        System.out.println("Capacity set for room " + roomId + ": " + capacity);
    }

    public int getRoomCapacity(String roomId) {
        return roomCapacity.getOrDefault(roomId, 0);
    }

    // Overloaded method - analyze peak periods
    public void analyzePeakPeriods() {
        System.out.println("Analyzing peak occupancy periods...");
        Map<LocalTime, Integer> timeOccupancy = new HashMap<>();

        for (OccupancyRecord record : occupancyRecords.values()) {
            LocalTime hour = record.time.withMinute(0).withSecond(0);
            timeOccupancy.put(hour, timeOccupancy.getOrDefault(hour, 0) + record.occupantCount);
        }

        // Find peak hour
        LocalTime peakHour = null;
        int maxOccupancy = 0;
        for (Map.Entry<LocalTime, Integer> entry : timeOccupancy.entrySet()) {
            if (entry.getValue() > maxOccupancy) {
                maxOccupancy = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        if (peakHour != null) {
            System.out.println("Peak occupancy hour: " + peakHour + " with " + maxOccupancy + " occupants");
        }
    }

    public void analyzePeakPeriods(String dateRange) {
        System.out.println("Analyzing peak periods for: " + dateRange);
        analyzePeakPeriods();
    }

    public void checkOvercrowding() {
        System.out.println("Checking for overcrowding...");
        for (OccupancyRecord record : occupancyRecords.values()) {
            int capacity = getRoomCapacity(record.roomId);
            if (capacity > 0 && record.occupantCount > capacity * 0.9) {
                System.out.println("WARNING: Room " + record.roomId + " is near capacity (" +
                                 record.occupantCount + "/" + capacity + ")");
            }
        }
    }

    public void optimizeUtilities() {
        System.out.println("Optimizing utilities based on occupancy patterns...");
        // Calculate average occupancy during peak hours
        int peakCount = 0;
        int totalRecords = 0;

        for (OccupancyRecord record : occupancyRecords.values()) {
            if (!record.time.isBefore(peakStartTime) && !record.time.isAfter(peakEndTime)) {
                peakCount += record.occupantCount;
                totalRecords++;
            }
        }

        double avgPeakOccupancy = totalRecords > 0 ? (double) peakCount / totalRecords : 0;
        System.out.println("Average peak occupancy: " + String.format("%.2f", avgPeakOccupancy));
        System.out.println("Recommendation: Increase HVAC and lighting during peak hours");
    }

    public double getOccupancyUtilization(String roomId) {
        Integer capacity = roomCapacity.get(roomId);
        if (capacity == null || capacity == 0) return 0.0;

        int totalOccupancy = 0;
        int count = 0;
        for (OccupancyRecord record : occupancyRecords.values()) {
            if (record.roomId.equals(roomId)) {
                totalOccupancy += record.occupantCount;
                count++;
            }
        }

        return count > 0 ? (double) totalOccupancy / (capacity * count) * 100 : 0;
    }

    public String generateOccupancyReport() {
        StringBuilder report = new StringBuilder();
        report.append("Occupancy Utilization Report:\n");
        report.append("Total Records: ").append(occupancyRecords.size()).append("\n");

        for (String roomId : roomCapacity.keySet()) {
            double utilization = getOccupancyUtilization(roomId);
            report.append("Room ").append(roomId).append(": ")
                  .append(String.format("%.1f", utilization)).append("% utilization\n");
        }

        return report.toString();
    }

    @Override
    public void performMaintenance() {
        System.out.println("Occupancy Monitor maintenance - calibrating sensors...");
    }

    // Getters
    public Map<String, OccupancyRecord> getOccupancyRecords() {
        return new HashMap<>(occupancyRecords);
    }

    public LocalTime getPeakStartTime() { return peakStartTime; }
    public LocalTime getPeakEndTime() { return peakEndTime; }

    public void setPeakHours(LocalTime start, LocalTime end) {
        this.peakStartTime = start;
        this.peakEndTime = end;
        System.out.println("Peak hours set: " + start + " - " + end);
    }
}
