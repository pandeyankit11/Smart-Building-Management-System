package com.smartbuilding.service;

import com.smartbuilding.exception.*;
import com.smartbuilding.model.*;
import com.smartbuilding.util.FileHandler;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * BuildingManager class - main service class that coordinates all building operations.
 * Demonstrates comprehensive integration of all system components.
 */
public class BuildingManager {
    private Building building;
    private AlertSystem alertSystem;
    private ReportGenerator reportGenerator;
    private FileHandler fileHandler;
    private User currentUser;
    private Scanner scanner;

    public BuildingManager() {
        this.building = new Building("Smart Complex", "123 Main Street");
        this.alertSystem = new AlertSystem();
        this.reportGenerator = new ReportGenerator(building,
            building.getSecuritySystem(),
            building.getOccupancyMonitor(),
            building.getLightingSystem());
        this.fileHandler = new FileHandler("data");
        this.scanner = new Scanner(System.in);
    }

    // Overloaded constructor for building initialization
    public BuildingManager(String buildingName, String address) {
        this();
        building.setBuildingName(buildingName);
        building.setAddress(address);
    }

    public void initializeDemoData() {
        System.out.println("Initializing demo data...");

        // Add floors
        Floor floor1 = new Floor("Ground Floor", 1);
        Floor floor2 = new Floor("First Floor", 2);
        building.addFloor(floor1);
        building.addFloor(floor2);

        // Add rooms to floor 1
        Room lobby = new Room("Lobby", 1, 50);
        Room securityOffice = new Room("Security Office", 1, 10);
        Room maintenanceRoom = new Room("Maintenance Room", 1, 5);
        floor1.addRoom(lobby);
        floor1.addRoom(securityOffice);
        floor1.addRoom(maintenanceRoom);

        // Add rooms to floor 2
        Room conferenceRoom = new Room("Conference Room", 2, 30);
        Room serverRoom = new Room("Server Room", 2, 5);
        floor2.addRoom(conferenceRoom);
        floor2.addRoom(serverRoom);

        // Add equipment
        Equipment.EquipmentSpecs cameraSpecs = new Equipment.EquipmentSpecs(
            "CAM-2024-HD", "SecureTech", 50.0, "2 years"
        );
        Equipment camera = new Equipment("Security Camera 1", "Security", "CAMERA", cameraSpecs, 50.0);
        securityOffice.addEquipment(camera);

        Equipment.EquipmentSpecs hvacSpecs = new Equipment.EquipmentSpecs(
            "HVAC-5000", "ClimatePro", 2000.0, "5 years"
        );
        Equipment hvac = new Equipment("HVAC Unit", lobby.getName(), "HVAC", hvacSpecs, 2000.0);
        lobby.addEquipment(hvac);

        Equipment.EquipmentSpecs lightSpecs = new Equipment.EquipmentSpecs(
            "LED-100W", "LightCorp", 100.0, "3 years"
        );
        Equipment light = new Equipment("Outdoor Light", "Exterior", "LIGHTING", lightSpecs, 100.0);
        lobby.addEquipment(light);

        // Initialize occupancy
        try {
            lobby.updateOccupancy(15);
            conferenceRoom.updateOccupancy(20);
        } catch (Exception e) {
            System.out.println("Error setting occupancy: " + e.getMessage());
        }

        // Add lighting system lights
        building.getLightingSystem().addMultipleLights(
            "Main Entrance", "Parking Lot A", "Parking Lot B",
            "Sidewalk North", "Sidewalk South", "Building Perimeter"
        );

        System.out.println("Demo data initialized successfully!");
    }

    // Overloaded login method
    public boolean login(String username, String password) throws Exception {
        // Simple user validation (in real system, would check database)
        if (username.equals("admin") && password.equals("admin123")) {
            currentUser = new Administrator(username, password);
            alertSystem.addListener((AlertListener) currentUser);
            System.out.println("Administrator logged in successfully!");
            return true;
        } else if (username.equals("staff") && password.equals("staff123")) {
            currentUser = new MaintenanceStaff(username, password, "Electrical");
            alertSystem.addListener((AlertListener) currentUser);
            System.out.println("Maintenance staff logged in successfully!");
            return true;
        } else if (username.equals("security") && password.equals("sec123")) {
            currentUser = new SecurityStaff(username, password, "Day Shift");
            alertSystem.addListener((AlertListener) currentUser);
            System.out.println("Security staff logged in successfully!");
            return true;
        } else if (username.equals("user") && password.equals("user123")) {
            currentUser = new GeneralUser(username, password, "A-101");
            System.out.println("General user logged in successfully!");
            return true;
        } else {
            throw new InvalidAccessException("Invalid credentials for user: " + username);
        }
    }

    // Another overload - using User object
    public boolean login(User user, String password) throws Exception {
        return login(user.getUsername(), password);
    }

    public void logout() {
        if (currentUser != null) {
            currentUser.logout();
            currentUser = null;
        }
    }

    // Overloaded method - add room with different parameters
    public void addRoom(String roomName, int capacity) {
        Room room = new Room(roomName, "Building", capacity);
        building.getFloors().get(0).addRoom(room); // Add to first floor
        System.out.println("Room added: " + roomName);
    }

    public void addRoom(String roomName, int capacity, String location) {
        Room room = new Room(roomName, location, capacity);
        building.getFloors().get(0).addRoom(room);
        System.out.println("Room added: " + roomName + " at " + location);
    }

    // Add equipment to room
    public void addEquipment(String roomId, String equipmentName, String equipmentType, double energyConsumption) throws EquipmentNotFoundException {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new EquipmentNotFoundException(roomId);
        }

        Equipment.EquipmentSpecs specs = new Equipment.EquipmentSpecs(
            "MODEL-" + System.currentTimeMillis() % 1000,
            "Generic",
            0.0,
            "1 year"
        );
        Equipment equipment = new Equipment(equipmentName, room.getName(), equipmentType, specs, energyConsumption);
        room.addEquipment(equipment);
        System.out.println("Equipment added: " + equipmentName + " to room: " + room.getName());
    }

    // Vararg method - add multiple equipment items at once
    public void addMultipleEquipment(String roomId, String[] equipmentNames, String equipmentType, double energyConsumption) throws EquipmentNotFoundException {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new EquipmentNotFoundException(roomId);
        }

        for (String eqName : equipmentNames) {
            Equipment.EquipmentSpecs specs = new Equipment.EquipmentSpecs(
                "MODEL-" + System.currentTimeMillis() % 1000,
                "Generic",
                0.0,
                "1 year"
            );
            Equipment equipment = new Equipment(eqName, room.getName(), equipmentType, specs, energyConsumption);
            room.addEquipment(equipment);
        }
        System.out.println(equipmentNames.length + " equipment items added to room: " + room.getName());
    }

    public void updateOccupancy(String roomId, int count) throws Exception {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new EquipmentNotFoundException(roomId);
        }
        room.updateOccupancy(count);
    }

    public void triggerAlert(String alertType, String message, String severity) {
        alertSystem.createAlert(alertType, message, severity);
        alertSystem.notifyListeners();
    }

    public void generateAndDisplayReport(String reportType) {
        String report = null;
        switch (reportType.toUpperCase()) {
            case "ENERGY":
                report = reportGenerator.generateEnergyReport();
                break;
            case "OCCUPANCY":
                report = reportGenerator.generateOccupancyReport();
                break;
            case "EQUIPMENT":
                report = reportGenerator.generateEquipmentReport();
                break;
            case "SECURITY":
                report = reportGenerator.generateSecurityReport();
                break;
            case "LIGHTING":
                report = reportGenerator.generateLightingReport();
                break;
            default:
                System.out.println("Unknown report type: " + reportType);
                return;
        }
        System.out.println("\n" + report);
    }

    // Vararg method - generate multiple reports at once
    public void generateMultipleReports(String... reportTypes) {
        String combinedReport = reportGenerator.generateCombinedReport(reportTypes);
        System.out.println("\n" + combinedReport);
    }

    // Overloaded with export option
    public void generateAndExportReport(String reportType, boolean export) throws FileOperationException {
        generateAndDisplayReport(reportType);
        if (export) {
            String report = getReportByType(reportType);
            if (report != null) {
                fileHandler.exportReportToFile(report, reportType.toLowerCase());
            }
        }
    }

    private String getReportByType(String reportType) {
        switch (reportType.toUpperCase()) {
            case "ENERGY": return reportGenerator.generateEnergyReport();
            case "OCCUPANCY": return reportGenerator.generateOccupancyReport();
            case "EQUIPMENT": return reportGenerator.generateEquipmentReport();
            case "SECURITY": return reportGenerator.generateSecurityReport();
            case "LIGHTING": return reportGenerator.generateLightingReport();
            default: return null;
        }
    }

    public void processActiveAlerts() {
        alertSystem.processAlerts();
    }

    public void saveData() throws FileOperationException {
        System.out.println("\nSaving system data...");
        fileHandler.logEvent("SYSTEM", "User " + (currentUser != null ? currentUser.getUsername() : "Unknown") + " saved data");
    }

    public void loadData() throws FileOperationException {
        System.out.println("\nLoading system data...");
        fileHandler.loadBuildingData();
    }

    public void runInteractiveMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== SMART BUILDING MANAGEMENT SYSTEM ===");
            System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() + " (" + currentUser.getRole() + ")" : "Not logged in"));
            System.out.println("\nOptions:");
            System.out.println("1. Login");
            System.out.println("2. Logout");
            System.out.println("3. Add Room");
            System.out.println("4. Add Equipment");
            System.out.println("5. Update Occupancy");
            System.out.println("6. Trigger Alert");
            System.out.println("7. View Active Alerts");
            System.out.println("8. Generate Reports");
            System.out.println("9. View Building Status");
            System.out.println("10. Lighting Control");
            System.out.println("11. Security Operations");
            System.out.println("12. Save Data");
            System.out.println("13. Load Data");
            System.out.println("14. Exit");
            System.out.print("\nEnter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            try {
                switch (choice) {
                    case 1:
                        handleLogin();
                        break;
                    case 2:
                        logout();
                        break;
                    case 3:
                        handleAddRoom();
                        break;
                    case 4:
                        handleAddEquipment();
                        break;
                    case 5:
                        handleUpdateOccupancy();
                        break;
                    case 6:
                        handleTriggerAlert();
                        break;
                    case 7:
                        processActiveAlerts();
                        break;
                    case 8:
                        handleGenerateReports();
                        break;
                    case 9:
                        System.out.println("\n" + building);
                        System.out.println("Total Floors: " + building.getFloors().size());
                        System.out.println("Total Energy: " + building.getTotalEnergyConsumption() + " kWh");
                        break;
                    case 10:
                        handleLightingControl();
                        break;
                    case 11:
                        handleSecurityOperations();
                        break;
                    case 12:
                        saveData();
                        break;
                    case 13:
                        loadData();
                        break;
                    case 14:
                        running = false;
                        System.out.println("Exiting system...");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                fileHandler.logEvent("ERROR", e.getMessage());
            }
        }
        scanner.close();
    }

    private void handleLogin() throws Exception {
        if (currentUser != null) {
            System.out.println("Already logged in as " + currentUser.getUsername());
            return;
        }
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        login(username, password);
    }

    private void handleAddRoom() throws InvalidAccessException {
        if (currentUser == null || !(currentUser instanceof Administrator)) {
            throw new InvalidAccessException(currentUser != null ? currentUser.getUsername() : "Unknown",
                                            "Add Room - Administrator access required");
        }
        System.out.print("Room name: ");
        String name = scanner.nextLine();
        System.out.print("Capacity: ");
        int capacity = scanner.nextInt();
        scanner.nextLine();
        addRoom(name, capacity);
    }

    private void handleAddEquipment() throws Exception {
        if (currentUser == null) {
            throw new InvalidAccessException("Add Equipment - Login required");
        }

        System.out.print("Room ID: ");
        String roomId = scanner.nextLine();
        System.out.print("Equipment name: ");
        String eqName = scanner.nextLine();
        System.out.print("Equipment type: ");
        String eqType = scanner.nextLine();
        System.out.print("Energy consumption (kWh): ");
        double energy = scanner.nextDouble();
        scanner.nextLine();

        addEquipment(roomId, eqName, eqType, energy);
    }

    private void handleUpdateOccupancy() throws Exception {
        if (currentUser == null) {
            throw new InvalidAccessException("Update Occupancy - Login required");
        }

        System.out.print("Room ID: ");
        String roomId = scanner.nextLine();
        System.out.print("New occupancy count: ");
        int count = scanner.nextInt();
        scanner.nextLine();

        updateOccupancy(roomId, count);
    }

    private void handleTriggerAlert() throws InvalidAccessException {
        if (currentUser == null) {
            throw new InvalidAccessException("Trigger Alert - Login required");
        }

        System.out.println("Alert Types:");
        System.out.println("1. EQUIPMENT_FAILURE");
        System.out.println("2. ENERGY_OVERUSE");
        System.out.println("3. SECURITY_BREACH");
        System.out.println("4. MAINTENANCE_REMINDER");
        System.out.print("Select type (1-4): ");
        int type = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Message: ");
        String message = scanner.nextLine();
        System.out.print("Severity (INFO/WARNING/HIGH/CRITICAL): ");
        String severity = scanner.nextLine();

        String alertType;
        switch (type) {
            case 1: alertType = "EQUIPMENT_FAILURE"; break;
            case 2: alertType = "ENERGY_OVERUSE"; break;
            case 3: alertType = "SECURITY_BREACH"; break;
            case 4: alertType = "MAINTENANCE_REMINDER"; break;
            default: alertType = "SYSTEM_ERROR";
        }

        triggerAlert(alertType, message, severity);
    }

    private void handleGenerateReports() throws InvalidAccessException {
        if (currentUser == null) {
            throw new InvalidAccessException("Generate Reports - Login required");
        }

        System.out.println("Report Types:");
        System.out.println("1. Energy");
        System.out.println("2. Occupancy");
        System.out.println("3. Equipment");
        System.out.println("4. Security");
        System.out.println("5. Lighting");
        System.out.println("6. Combined (multiple)");
        System.out.print("Select type (1-6): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                generateAndDisplayReport("ENERGY");
                break;
            case 2:
                generateAndDisplayReport("OCCUPANCY");
                break;
            case 3:
                generateAndDisplayReport("EQUIPMENT");
                break;
            case 4:
                generateAndDisplayReport("SECURITY");
                break;
            case 5:
                generateAndDisplayReport("LIGHTING");
                break;
            case 6:
                System.out.print("Enter report types (comma separated): ");
                String[] types = scanner.nextLine().split(",");
                generateMultipleReports(types);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private void handleLightingControl() throws InvalidAccessException {
        if (currentUser == null) {
            throw new InvalidAccessException("Lighting Control - Login required");
        }

        System.out.println("1. Turn all lights ON");
        System.out.println("2. Turn all lights OFF");
        System.out.println("3. View lighting status");
        System.out.print("Select: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        LightingSystem lighting = building.getLightingSystem();
        switch (choice) {
            case 1:
                lighting.controlAllLights(true);
                break;
            case 2:
                lighting.controlAllLights(false);
                break;
            case 3:
                System.out.println("\nLighting System Status:");
                System.out.println("Total Lights: " + lighting.getLightCount());
                System.out.println("Energy Consumption: " + lighting.getTotalEnergyConsumption() + " kWh");
                for (LightingSystem.Light light : lighting.getLights()) {
                    System.out.println("  " + light);
                }
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private void handleSecurityOperations() throws InvalidAccessException {
        if (currentUser == null || !(currentUser instanceof SecurityStaff || currentUser instanceof Administrator)) {
            throw new InvalidAccessException(currentUser != null ? currentUser.getUsername() : "Unknown",
                                            "Security Operations - Security staff or Administrator access required");
        }

        SecuritySystem security = building.getSecuritySystem();
        System.out.println("1. Log Access");
        System.out.println("2. Report Incident");
        System.out.println("3. Trigger Alarm");
        System.out.println("4. View Incidents");
        System.out.println("5. View Active Alarms");
        System.out.print("Select: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.print("User ID: ");
                String userId = scanner.nextLine();
                System.out.print("Location: ");
                String location = scanner.nextLine();
                security.logAccess(userId, location);
                break;
            case 2:
                System.out.print("Incident type: ");
                String type = scanner.nextLine();
                System.out.print("Description: ");
                String desc = scanner.nextLine();
                System.out.print("Severity (INFO/WARNING/HIGH/CRITICAL): ");
                String severity = scanner.nextLine();
                security.reportIncident(type, desc, severity);
                break;
            case 3:
                System.out.print("Alarm type: ");
                String alarmType = scanner.nextLine();
                System.out.print("Location: ");
                String alarmLoc = scanner.nextLine();
                security.triggerAlarm(alarmType, alarmLoc);
                break;
            case 4:
                System.out.println("\nIncidents:");
                for (SecuritySystem.Incident inc : security.getIncidents()) {
                    System.out.println("  " + inc);
                }
                break;
            case 5:
                System.out.println("\nActive Alarms:");
                for (SecuritySystem.Alarm alarm : security.getAlarms()) {
                    if (alarm.isActive()) {
                        System.out.println("  " + alarm);
                    }
                }
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private Room findRoomById(String roomId) {
        for (Floor floor : building.getFloors()) {
            Room room = floor.getRoomById(roomId);
            if (room != null) {
                return room;
            }
        }
        return null;
    }

    // Getter for testing
    public Building getBuilding() {
        return building;
    }
}
