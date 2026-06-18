package com.smartbuilding.service;

import com.smartbuilding.exception.*;
import com.smartbuilding.model.*;
import com.smartbuilding.util.FileHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
        this("Smart Complex", "123 Main Street", "data", new Scanner(System.in));
    }

    public BuildingManager(String buildingName, String address, String dataDirectory, Scanner scanner) {
        this.building = new Building("Smart Complex", "123 Main Street");
        this.building.setBuildingName(buildingName);
        this.building.setAddress(address);
        this.alertSystem = new AlertSystem();
        rebuildReportGenerator();
        this.fileHandler = new FileHandler(dataDirectory);
        this.scanner = scanner;
    }

    // Overloaded constructor for building initialization
    public BuildingManager(String buildingName, String address) {
        this(buildingName, address, "data", new Scanner(System.in));
    }

    public void initializeDemoData() {
        if (!building.getFloors().isEmpty()) {
            System.out.println("Demo data already initialized; skipping duplicate setup.");
            return;
        }
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
    public boolean login(String username, String password) throws InvalidAccessException {
        // Simple user validation (in real system, would check database)
        if (username.equals("admin") && password.equals("admin123")) {
            currentUser = new Administrator(username, password);
            currentUser.login(password);
            alertSystem.addListener((AlertListener) currentUser);
            System.out.println("Administrator logged in successfully!");
            return true;
        } else if (username.equals("staff") && password.equals("staff123")) {
            currentUser = new MaintenanceStaff(username, password, "Electrical");
            currentUser.login(password);
            alertSystem.addListener((AlertListener) currentUser);
            System.out.println("Maintenance staff logged in successfully!");
            return true;
        } else if (username.equals("security") && password.equals("sec123")) {
            currentUser = new SecurityStaff(username, password, "Day Shift");
            currentUser.login(password);
            alertSystem.addListener((AlertListener) currentUser);
            System.out.println("Security staff logged in successfully!");
            return true;
        } else if (username.equals("user") && password.equals("user123")) {
            currentUser = new GeneralUser(username, password, "A-101");
            currentUser.login(password);
            System.out.println("General user logged in successfully!");
            return true;
        } else {
            throw new InvalidAccessException("Invalid credentials for user: " + username);
        }
    }

    // Another overload - using User object
    public boolean login(User user, String password) throws InvalidAccessException {
        return login(user.getUsername(), password);
    }

    public void logout() {
        if (currentUser != null) {
            if (currentUser instanceof AlertListener) {
                alertSystem.removeListener((AlertListener) currentUser);
            }
            currentUser.logout();
            currentUser = null;
        }
    }

    public void updateBuildingDetails(String name, String address) throws InvalidAccessException {
        requireRole("Edit Building", "ADMINISTRATOR");
        building.setBuildingName(name);
        building.setAddress(address);
        System.out.println("Building details updated.");
    }

    public void addFloor(String floorName, int floorNumber) throws InvalidAccessException {
        requireRole("Add Floor", "ADMINISTRATOR");
        building.addFloor(new Floor(floorName, floorNumber));
    }

    public void updateFloor(int floorNumber, String floorName)
            throws InvalidAccessException, InvalidInputException {
        requireRole("Edit Floor", "ADMINISTRATOR");
        Floor floor = building.getFloorByNumber(floorNumber);
        if (floor == null) {
            throw new InvalidInputException("floorNumber", String.valueOf(floorNumber), "existing floor number");
        }
        floor.setName(floorName);
        System.out.println("Floor " + floorNumber + " updated.");
    }

    public void deleteFloor(int floorNumber) throws InvalidAccessException, InvalidInputException {
        requireRole("Delete Floor", "ADMINISTRATOR");
        if (!building.removeFloor(floorNumber)) {
            throw new InvalidInputException("floorNumber", String.valueOf(floorNumber), "existing floor number");
        }
    }

    // Overloaded method - add room with different parameters
    public void addRoom(String roomName, int capacity) throws InvalidAccessException {
        requireRole("Add Room", "ADMINISTRATOR");
        ensureBuildingHasFloor();
        Room room = new Room(roomName, "Building", capacity);
        building.getFloors().get(0).addRoom(room); // Add to first floor
        System.out.println("Room added: " + roomName);
    }

    public void addRoom(String roomName, int capacity, String location) throws InvalidAccessException {
        requireRole("Add Room", "ADMINISTRATOR");
        ensureBuildingHasFloor();
        Room room = new Room(roomName, location, capacity);
        building.getFloors().get(0).addRoom(room);
        System.out.println("Room added: " + roomName + " at " + location);
    }

    public void addRoom(int floorNumber, String roomName, String location, int capacity)
            throws InvalidAccessException, InvalidInputException {
        requireRole("Add Room", "ADMINISTRATOR");
        Floor floor = building.getFloorByNumber(floorNumber);
        if (floor == null) {
            throw new InvalidInputException("floorNumber", String.valueOf(floorNumber), "existing floor number");
        }
        floor.addRoom(new Room(roomName, location, capacity));
    }

    public void updateRoom(String roomId, String name, String location, int capacity)
            throws EquipmentNotFoundException, InvalidAccessException {
        requireRole("Edit Room", "ADMINISTRATOR");
        Room room = requireRoom(roomId);
        room.updateDetails(name, location, capacity);
    }

    public void deleteRoom(String roomId) throws EquipmentNotFoundException, InvalidAccessException {
        requireRole("Delete Room", "ADMINISTRATOR");
        for (Floor floor : building.getFloors()) {
            if (floor.removeRoom(roomId)) {
                return;
            }
        }
        throw new EquipmentNotFoundException(roomId);
    }

    // Add equipment to room
    public void addEquipment(String roomId, String equipmentName, String equipmentType, double energyConsumption)
            throws EquipmentNotFoundException, InvalidAccessException {
        requireRole("Add Equipment", "ADMINISTRATOR", "MAINTENANCE");
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
    public void addMultipleEquipment(String roomId, String[] equipmentNames, String equipmentType, double energyConsumption)
            throws EquipmentNotFoundException, InvalidAccessException {
        requireRole("Add Equipment", "ADMINISTRATOR", "MAINTENANCE");
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

    public void updateEquipment(String equipmentId, String name, String location, String type,
                                double energyConsumption, String status)
            throws EquipmentNotFoundException, InvalidAccessException, InvalidInputException {
        requireRole("Edit Equipment", "ADMINISTRATOR", "MAINTENANCE");
        Equipment equipment = requireEquipment(equipmentId);
        equipment.updateDetails(name, location, type, energyConsumption, status);
    }

    public void deleteEquipment(String equipmentId)
            throws EquipmentNotFoundException, InvalidAccessException {
        requireRole("Delete Equipment", "ADMINISTRATOR", "MAINTENANCE");
        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                if (room.removeEquipment(equipmentId)) {
                    return;
                }
            }
        }
        throw new EquipmentNotFoundException(equipmentId);
    }

    public void updateOccupancy(String roomId, int count)
            throws EquipmentNotFoundException, InvalidAccessException, InvalidInputException {
        requireRole("Update Occupancy", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new EquipmentNotFoundException(roomId);
        }
        room.updateOccupancy(count);
    }

    public void triggerAlert(String alertType, String message, String severity) throws InvalidAccessException {
        requireRole("Trigger Alert", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
        alertSystem.createAlert(alertType, message, severity);
        alertSystem.notifyListeners();
    }

    public void addLight(String location) throws InvalidAccessException {
        requireRole("Add Light", "ADMINISTRATOR", "MAINTENANCE");
        building.getLightingSystem().addLight(location);
    }

    public void updateLight(String lightId, String location, int brightness)
            throws InvalidAccessException, InvalidInputException {
        requireRole("Edit Light", "ADMINISTRATOR", "MAINTENANCE");
        LightingSystem.Light light = requireLight(lightId);
        light.setLocation(location);
        light.setBrightness(brightness);
    }

    public void deleteLight(String lightId) throws InvalidAccessException, InvalidInputException {
        requireRole("Delete Light", "ADMINISTRATOR", "MAINTENANCE");
        if (!building.getLightingSystem().removeLight(lightId)) {
            throw new InvalidInputException("lightId", lightId, "existing light ID");
        }
    }

    public void acknowledgeAlert(String alertId) throws InvalidAccessException, InvalidInputException {
        requireRole("Acknowledge Alert", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
        Alert alert = requireActiveAlert(alertId);
        alert.acknowledge(currentUser.getUsername());
    }

    public void updateAlert(String alertId, String message, String severity)
            throws InvalidAccessException, InvalidInputException {
        requireRole("Edit Alert", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
        requireActiveAlert(alertId).update(message, severity);
    }

    public void resolveAlert(String alertId) throws InvalidAccessException, InvalidInputException {
        requireRole("Resolve Alert", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
        alertSystem.resolveAlert(alertId);
    }

    public void deleteAlert(String alertId) throws InvalidAccessException, InvalidInputException {
        requireRole("Delete Alert", "ADMINISTRATOR");
        if (!alertSystem.deleteAlert(alertId)) {
            throw new InvalidInputException("alertId", alertId, "existing alert ID");
        }
        System.out.println("Alert deleted: " + alertId);
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

    public void saveData() throws FileOperationException, InvalidAccessException {
        requireRole("Save Data", "ADMINISTRATOR");
        System.out.println("\nSaving system data...");
        fileHandler.saveBuildingData(building, alertSystem);
        fileHandler.logEvent("SYSTEM", "User " + (currentUser != null ? currentUser.getUsername() : "Unknown") + " saved data");
    }

    public void loadData() throws FileOperationException, InvalidAccessException {
        requireRole("Load Data", "ADMINISTRATOR");
        System.out.println("\nLoading system data...");
        FileHandler.SavedState state = fileHandler.loadBuildingData();
        this.building = state.getBuilding();
        this.alertSystem = state.getAlertSystem();
        this.currentUser = null;
        rebuildReportGenerator();
    }

    public void runInteractiveMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== SMART BUILDING MANAGEMENT SYSTEM ===");
            System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() + " (" + currentUser.getRole() + ")" : "Not logged in"));
            System.out.println("\nMain Menu:");
            System.out.println("1. Login");
            System.out.println("2. Logout");
            System.out.println("3. Building & Equipment");
            System.out.println("4. Occupancy");
            System.out.println("5. Lighting");
            System.out.println("6. Security");
            System.out.println("7. Alerts");
            System.out.println("8. Reports");
            System.out.println("9. View Complete Status");
            System.out.println("10. Save Data");
            System.out.println("11. Load Data");
            System.out.println("12. Exit");
            System.out.print("\nEnter choice: ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nInput closed. Exiting system...");
                break;
            }

            try {
                int choice = readInt();
                switch (choice) {
                    case 1:
                        handleLogin();
                        break;
                    case 2:
                        logout();
                        break;
                    case 3:
                        handleBuildingManagement();
                        break;
                    case 4:
                        handleOccupancyManagement();
                        break;
                    case 5:
                        handleLightingManagement();
                        break;
                    case 6:
                        handleSecurityManagement();
                        break;
                    case 7:
                        handleAlertManagement();
                        break;
                    case 8:
                        handleReportManagement();
                        break;
                    case 9:
                        requireLoggedIn("View Complete Status");
                        displayBuildingDetails();
                        break;
                    case 10:
                        saveData();
                        break;
                    case 11:
                        loadData();
                        break;
                    case 12:
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

    private void handleLogin() throws InvalidAccessException {
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

    private void handleBuildingManagement()
            throws InvalidAccessException, InvalidInputException, EquipmentNotFoundException {
        requireLoggedIn("Building & Equipment");
        System.out.println("\n--- Building & Equipment ---");
        System.out.println("1. View all");
        System.out.println("2. Edit building details");
        System.out.println("3. Add floor");
        System.out.println("4. Edit floor name");
        System.out.println("5. Delete floor");
        System.out.println("6. Add room");
        System.out.println("7. Edit room");
        System.out.println("8. Delete room");
        System.out.println("9. Add equipment");
        System.out.println("10. Edit equipment");
        System.out.println("11. Delete equipment");
        System.out.println("12. Back");
        System.out.print("Select: ");

        switch (readInt()) {
            case 1:
                displayBuildingDetails();
                break;
            case 2:
                System.out.print("New building name: ");
                String buildingName = scanner.nextLine();
                System.out.print("New address: ");
                updateBuildingDetails(buildingName, scanner.nextLine());
                break;
            case 3:
                System.out.print("Floor name: ");
                String floorName = scanner.nextLine();
                System.out.print("Floor number: ");
                addFloor(floorName, readInt());
                break;
            case 4:
                Floor floorToEdit = selectFloor();
                System.out.print("New floor name: ");
                updateFloor(floorToEdit.getFloorNumber(), scanner.nextLine());
                break;
            case 5:
                deleteFloor(selectFloor().getFloorNumber());
                break;
            case 6:
                Floor roomFloor = selectFloor();
                System.out.print("Room name: ");
                String roomName = scanner.nextLine();
                System.out.print("Room location: ");
                String roomLocation = scanner.nextLine();
                System.out.print("Room capacity: ");
                addRoom(roomFloor.getFloorNumber(), roomName, roomLocation, readInt());
                break;
            case 7:
                Room roomToEdit = selectRoom();
                System.out.print("New room name: ");
                String newRoomName = scanner.nextLine();
                System.out.print("New room location: ");
                String newRoomLocation = scanner.nextLine();
                System.out.print("New room capacity: ");
                updateRoom(roomToEdit.getComponentId(), newRoomName, newRoomLocation, readInt());
                break;
            case 8:
                deleteRoom(selectRoom().getComponentId());
                break;
            case 9:
                Room equipmentRoom = selectRoom();
                System.out.print("Equipment name: ");
                String equipmentName = scanner.nextLine();
                System.out.print("Equipment type: ");
                String equipmentType = scanner.nextLine();
                System.out.print("Energy consumption (kWh): ");
                addEquipment(equipmentRoom.getComponentId(), equipmentName, equipmentType, readDouble());
                break;
            case 10:
                Equipment equipment = selectEquipment();
                System.out.print("New equipment name: ");
                String newEquipmentName = scanner.nextLine();
                System.out.print("New location: ");
                String newEquipmentLocation = scanner.nextLine();
                System.out.print("New type: ");
                String newEquipmentType = scanner.nextLine();
                System.out.print("New energy consumption (kWh): ");
                double energy = readDouble();
                System.out.print("Status (OPERATIONAL/MAINTENANCE/MALFUNCTIONING): ");
                updateEquipment(equipment.getComponentId(), newEquipmentName, newEquipmentLocation,
                        newEquipmentType, energy, scanner.nextLine());
                break;
            case 11:
                deleteEquipment(selectEquipment().getComponentId());
                break;
            case 12:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void handleOccupancyManagement()
            throws InvalidAccessException, InvalidInputException, EquipmentNotFoundException {
        requireLoggedIn("Occupancy");
        System.out.println("\n--- Occupancy ---");
        System.out.println("1. View room occupancy");
        System.out.println("2. Set occupancy");
        System.out.println("3. Add one occupant");
        System.out.println("4. Remove one occupant");
        System.out.println("5. Back");
        System.out.print("Select: ");

        switch (readInt()) {
            case 1:
                displayRooms();
                break;
            case 2:
                Room room = selectRoom();
                System.out.print("New occupancy: ");
                updateOccupancy(room.getComponentId(), readInt());
                break;
            case 3:
                requireRole("Increase Occupancy", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
                selectRoom().incrementOccupancy();
                break;
            case 4:
                requireRole("Decrease Occupancy", "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
                selectRoom().decrementOccupancy();
                break;
            case 5:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void handleLightingManagement() throws InvalidAccessException, InvalidInputException {
        requireLoggedIn("Lighting");
        LightingSystem lighting = building.getLightingSystem();
        System.out.println("\n--- Lighting ---");
        System.out.println("1. View lights");
        System.out.println("2. Add light");
        System.out.println("3. Edit light");
        System.out.println("4. Delete light");
        System.out.println("5. Turn one light ON");
        System.out.println("6. Turn one light OFF");
        System.out.println("7. Turn all lights ON");
        System.out.println("8. Turn all lights OFF");
        System.out.println("9. Set automatic schedule");
        System.out.println("10. Run automatic control now");
        System.out.println("11. Back");
        System.out.print("Select: ");

        switch (readInt()) {
            case 1:
                displayLights();
                break;
            case 2:
                System.out.print("Light location: ");
                addLight(scanner.nextLine());
                break;
            case 3:
                LightingSystem.Light light = selectLight();
                System.out.print("New location: ");
                String location = scanner.nextLine();
                System.out.print("Brightness (0-100): ");
                updateLight(light.getLightId(), location, readInt());
                break;
            case 4:
                deleteLight(selectLight().getLightId());
                break;
            case 5:
                requireRole("Turn Light On", "ADMINISTRATOR", "MAINTENANCE");
                selectLight().turnOn();
                break;
            case 6:
                requireRole("Turn Light Off", "ADMINISTRATOR", "MAINTENANCE");
                selectLight().turnOff();
                break;
            case 7:
                requireRole("Turn All Lights On", "ADMINISTRATOR", "MAINTENANCE");
                lighting.controlAllLights(true);
                break;
            case 8:
                requireRole("Turn All Lights Off", "ADMINISTRATOR", "MAINTENANCE");
                lighting.controlAllLights(false);
                break;
            case 9:
                requireRole("Set Lighting Schedule", "ADMINISTRATOR", "MAINTENANCE");
                System.out.print("ON time (HH:mm): ");
                LocalTime onTime = readTime();
                System.out.print("OFF time (HH:mm): ");
                lighting.setSchedule(onTime, readTime());
                break;
            case 10:
                requireRole("Run Automatic Lighting", "ADMINISTRATOR", "MAINTENANCE");
                lighting.autoControlBasedOnTime();
                break;
            case 11:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void handleSecurityManagement() throws InvalidAccessException, InvalidInputException {
        requireRole("Security", "ADMINISTRATOR", "SECURITY");
        SecuritySystem security = building.getSecuritySystem();
        System.out.println("\n--- Security ---");
        System.out.println("1. View access logs");
        System.out.println("2. Add access log");
        System.out.println("3. Delete access log");
        System.out.println("4. View incidents");
        System.out.println("5. Add incident");
        System.out.println("6. Update incident status");
        System.out.println("7. Delete incident");
        System.out.println("8. View alarms");
        System.out.println("9. Trigger alarm");
        System.out.println("10. Deactivate alarm");
        System.out.println("11. Delete alarm");
        System.out.println("12. Toggle surveillance");
        System.out.println("13. Back");
        System.out.print("Select: ");

        switch (readInt()) {
            case 1:
                displayAccessLogs();
                break;
            case 2:
                System.out.print("User ID: ");
                String userId = scanner.nextLine();
                System.out.print("Location: ");
                String accessLocation = scanner.nextLine();
                System.out.print("Access type (ENTRY/EXIT): ");
                String accessType = scanner.nextLine();
                System.out.print("Authorized (yes/no): ");
                security.logAccess(userId, accessLocation, accessType, readYesNo());
                break;
            case 3:
                if (!security.deleteAccessLog(selectAccessLog().getLogId())) {
                    throw new InvalidInputException("accessLog", "", "existing access log");
                }
                System.out.println("Access log deleted.");
                break;
            case 4:
                displayIncidents();
                break;
            case 5:
                System.out.print("Incident type: ");
                String incidentType = scanner.nextLine();
                System.out.print("Description: ");
                String description = scanner.nextLine();
                System.out.print("Severity (INFO/WARNING/HIGH/CRITICAL): ");
                security.reportIncident(incidentType, description, scanner.nextLine());
                break;
            case 6:
                SecuritySystem.Incident incident = selectIncident();
                System.out.print("Status (OPEN/INVESTIGATING/RESOLVED): ");
                incident.updateStatus(scanner.nextLine());
                break;
            case 7:
                if (!security.deleteIncident(selectIncident().getIncidentId())) {
                    throw new InvalidInputException("incident", "", "existing incident");
                }
                System.out.println("Incident deleted.");
                break;
            case 8:
                displayAlarms();
                break;
            case 9:
                System.out.print("Alarm type: ");
                String alarmType = scanner.nextLine();
                System.out.print("Location: ");
                security.triggerAlarm(alarmType, scanner.nextLine());
                break;
            case 10:
                security.deactivateAlarm(selectAlarm().getAlarmId());
                break;
            case 11:
                if (!security.deleteAlarm(selectAlarm().getAlarmId())) {
                    throw new InvalidInputException("alarm", "", "existing alarm");
                }
                System.out.println("Alarm deleted.");
                break;
            case 12:
                security.setSurveillanceActive(!security.isSurveillanceActive());
                System.out.println("Surveillance is now "
                        + (security.isSurveillanceActive() ? "ACTIVE" : "INACTIVE"));
                break;
            case 13:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void handleAlertManagement() throws InvalidAccessException, InvalidInputException {
        requireLoggedIn("Alerts");
        System.out.println("\n--- Alerts ---");
        System.out.println("1. View active alerts");
        System.out.println("2. View resolved alerts");
        System.out.println("3. Add alert");
        System.out.println("4. Edit active alert");
        System.out.println("5. Acknowledge active alert");
        System.out.println("6. Resolve active alert");
        System.out.println("7. Delete alert");
        System.out.println("8. Back");
        System.out.print("Select: ");

        switch (readInt()) {
            case 1:
                displayAlerts(alertSystem.getActiveAlerts(), "Active Alerts");
                break;
            case 2:
                displayAlerts(alertSystem.getResolvedAlerts(), "Resolved Alerts");
                break;
            case 3:
                System.out.print("Alert type: ");
                String type = scanner.nextLine();
                System.out.print("Message: ");
                String message = scanner.nextLine();
                System.out.print("Severity (INFO/WARNING/HIGH/CRITICAL): ");
                triggerAlert(type, message, scanner.nextLine());
                break;
            case 4:
                Alert alertToEdit = selectActiveAlert();
                System.out.print("New message: ");
                String newMessage = scanner.nextLine();
                System.out.print("New severity: ");
                updateAlert(alertToEdit.getAlertId(), newMessage, scanner.nextLine());
                break;
            case 5:
                acknowledgeAlert(selectActiveAlert().getAlertId());
                break;
            case 6:
                resolveAlert(selectActiveAlert().getAlertId());
                break;
            case 7:
                deleteAlert(selectAnyAlert().getAlertId());
                break;
            case 8:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void handleReportManagement()
            throws InvalidAccessException, InvalidInputException, FileOperationException {
        requireLoggedIn("Reports");
        System.out.println("\n--- Reports ---");
        System.out.println("1. Energy report");
        System.out.println("2. Occupancy report");
        System.out.println("3. Equipment report");
        System.out.println("4. Security report");
        System.out.println("5. Lighting report");
        System.out.println("6. Combined report");
        System.out.println("7. Export a report");
        System.out.println("8. Back");
        System.out.print("Select: ");

        int choice = readInt();
        if (choice >= 1 && choice <= 5) {
            generateAndDisplayReport(reportTypeForChoice(choice));
        } else if (choice == 6) {
            generateMultipleReports("ENERGY", "OCCUPANCY", "EQUIPMENT", "SECURITY", "LIGHTING");
        } else if (choice == 7) {
            System.out.print("Report number to export (1-5): ");
            generateAndExportReport(reportTypeForChoice(readInt()), true);
        } else if (choice != 8) {
            System.out.println("Invalid choice.");
        }
    }

    private void displayBuildingDetails() {
        System.out.println("\n=== COMPLETE BUILDING STATUS ===");
        System.out.println(building);
        System.out.println("Energy: " + building.getTotalEnergyConsumption() + " kWh");
        for (Floor floor : building.getFloors()) {
            System.out.println("\nFloor " + floor.getFloorNumber() + ": " + floor.getName()
                    + " [" + floor.getComponentId() + "]");
            for (Room room : floor.getRooms()) {
                System.out.println("  Room: " + room.getName() + " [" + room.getComponentId() + "]"
                        + ", location=" + room.getLocation() + ", occupancy="
                        + room.getCurrentOccupancy() + "/" + room.getCapacity());
                for (Equipment equipment : room.getEquipmentList()) {
                    System.out.println("    Equipment: " + equipment.getName() + " ["
                            + equipment.getComponentId() + "], type=" + equipment.getEquipmentType()
                            + ", status=" + equipment.getStatus() + ", energy="
                            + equipment.getEnergyConsumption() + " kWh");
                }
            }
        }
    }

    private void displayRooms() {
        List<Room> rooms = allRooms();
        System.out.println("\nRooms:");
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            System.out.println((i + 1) + ". " + room.getName() + " [" + room.getComponentId()
                    + "] - " + room.getCurrentOccupancy() + "/" + room.getCapacity());
        }
        if (rooms.isEmpty()) {
            System.out.println("No rooms available.");
        }
    }

    private void displayLights() {
        List<LightingSystem.Light> lights = building.getLightingSystem().getLights();
        System.out.println("\nLights:");
        for (int i = 0; i < lights.size(); i++) {
            System.out.println((i + 1) + ". " + lights.get(i));
        }
        if (lights.isEmpty()) {
            System.out.println("No lights available.");
        }
    }

    private void displayAccessLogs() {
        List<SecuritySystem.AccessLog> logs = building.getSecuritySystem().getAccessLogs();
        System.out.println("\nAccess Logs:");
        for (int i = 0; i < logs.size(); i++) {
            System.out.println((i + 1) + ". " + logs.get(i));
        }
        if (logs.isEmpty()) {
            System.out.println("No access logs available.");
        }
    }

    private void displayIncidents() {
        List<SecuritySystem.Incident> incidents = building.getSecuritySystem().getIncidents();
        System.out.println("\nIncidents:");
        for (int i = 0; i < incidents.size(); i++) {
            System.out.println((i + 1) + ". " + incidents.get(i));
        }
        if (incidents.isEmpty()) {
            System.out.println("No incidents available.");
        }
    }

    private void displayAlarms() {
        List<SecuritySystem.Alarm> alarms = building.getSecuritySystem().getAlarms();
        System.out.println("\nAlarms:");
        for (int i = 0; i < alarms.size(); i++) {
            System.out.println((i + 1) + ". " + alarms.get(i));
        }
        if (alarms.isEmpty()) {
            System.out.println("No alarms available.");
        }
    }

    private void displayAlerts(List<Alert> alerts, String title) {
        System.out.println("\n" + title + ":");
        for (int i = 0; i < alerts.size(); i++) {
            System.out.println((i + 1) + ". " + alerts.get(i));
        }
        if (alerts.isEmpty()) {
            System.out.println("No alerts available.");
        }
    }

    private Floor selectFloor() throws InvalidInputException {
        List<Floor> floors = building.getFloors();
        System.out.println("\nFloors:");
        for (int i = 0; i < floors.size(); i++) {
            System.out.println((i + 1) + ". Floor " + floors.get(i).getFloorNumber()
                    + " - " + floors.get(i).getName());
        }
        return selectFrom(floors, "floor");
    }

    private Room selectRoom() throws InvalidInputException {
        List<Room> rooms = allRooms();
        displayRooms();
        return selectFrom(rooms, "room");
    }

    private Equipment selectEquipment() throws InvalidInputException {
        List<Equipment> equipment = allEquipment();
        System.out.println("\nEquipment:");
        for (int i = 0; i < equipment.size(); i++) {
            Equipment item = equipment.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " [" + item.getComponentId()
                    + "] - " + item.getEquipmentType() + "/" + item.getStatus());
        }
        return selectFrom(equipment, "equipment");
    }

    private LightingSystem.Light selectLight() throws InvalidInputException {
        List<LightingSystem.Light> lights = building.getLightingSystem().getLights();
        displayLights();
        return selectFrom(lights, "light");
    }

    private SecuritySystem.AccessLog selectAccessLog() throws InvalidInputException {
        List<SecuritySystem.AccessLog> logs = building.getSecuritySystem().getAccessLogs();
        displayAccessLogs();
        return selectFrom(logs, "access log");
    }

    private SecuritySystem.Incident selectIncident() throws InvalidInputException {
        List<SecuritySystem.Incident> incidents = building.getSecuritySystem().getIncidents();
        displayIncidents();
        return selectFrom(incidents, "incident");
    }

    private SecuritySystem.Alarm selectAlarm() throws InvalidInputException {
        List<SecuritySystem.Alarm> alarms = building.getSecuritySystem().getAlarms();
        displayAlarms();
        return selectFrom(alarms, "alarm");
    }

    private Alert selectActiveAlert() throws InvalidInputException {
        List<Alert> alerts = alertSystem.getActiveAlerts();
        displayAlerts(alerts, "Active Alerts");
        return selectFrom(alerts, "active alert");
    }

    private Alert selectAnyAlert() throws InvalidInputException {
        List<Alert> alerts = new ArrayList<>(alertSystem.getActiveAlerts());
        alerts.addAll(alertSystem.getResolvedAlerts());
        displayAlerts(alerts, "All Alerts");
        return selectFrom(alerts, "alert");
    }

    private <T> T selectFrom(List<T> items, String itemName) throws InvalidInputException {
        if (items.isEmpty()) {
            throw new InvalidInputException("selection", "", "at least one " + itemName);
        }
        System.out.print("Select " + itemName + " number: ");
        int selection = readInt();
        if (selection < 1 || selection > items.size()) {
            throw new InvalidInputException("selection", String.valueOf(selection),
                    "1-" + items.size());
        }
        return items.get(selection - 1);
    }

    private List<Room> allRooms() {
        List<Room> rooms = new ArrayList<>();
        for (Floor floor : building.getFloors()) {
            rooms.addAll(floor.getRooms());
        }
        return rooms;
    }

    private List<Equipment> allEquipment() {
        List<Equipment> equipment = new ArrayList<>();
        for (Room room : allRooms()) {
            equipment.addAll(room.getEquipmentList());
        }
        return equipment;
    }

    private String reportTypeForChoice(int choice) throws InvalidInputException {
        switch (choice) {
            case 1: return "ENERGY";
            case 2: return "OCCUPANCY";
            case 3: return "EQUIPMENT";
            case 4: return "SECURITY";
            case 5: return "LIGHTING";
            default:
                throw new InvalidInputException("report", String.valueOf(choice), "1-5");
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

    private Room requireRoom(String roomId) throws EquipmentNotFoundException {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new EquipmentNotFoundException(roomId);
        }
        return room;
    }

    private Equipment requireEquipment(String equipmentId) throws EquipmentNotFoundException {
        for (Room room : allRooms()) {
            Equipment equipment = room.getEquipmentById(equipmentId);
            if (equipment != null) {
                return equipment;
            }
        }
        throw new EquipmentNotFoundException(equipmentId);
    }

    private LightingSystem.Light requireLight(String lightId) throws InvalidInputException {
        LightingSystem.Light light = building.getLightingSystem().getLightById(lightId);
        if (light == null) {
            throw new InvalidInputException("lightId", lightId, "existing light ID");
        }
        return light;
    }

    private Alert requireActiveAlert(String alertId) throws InvalidInputException {
        Alert alert = alertSystem.getActiveAlertById(alertId);
        if (alert == null) {
            throw new InvalidInputException("alertId", alertId, "active alert ID");
        }
        return alert;
    }

    // Getter for testing
    public Building getBuilding() {
        return building;
    }

    public AlertSystem getAlertSystem() {
        return alertSystem;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void rebuildReportGenerator() {
        this.reportGenerator = new ReportGenerator(building,
                building.getSecuritySystem(),
                building.getOccupancyMonitor(),
                building.getLightingSystem());
    }

    private void requireRole(String operation, String... allowedRoles) throws InvalidAccessException {
        if (currentUser == null) {
            throw new InvalidAccessException("Login required for operation: " + operation);
        }
        for (String role : allowedRoles) {
            if (role.equals(currentUser.getRole())) {
                return;
            }
        }
        throw new InvalidAccessException(currentUser.getUsername(), operation);
    }

    private void requireLoggedIn(String operation) throws InvalidAccessException {
        if (currentUser == null) {
            throw new InvalidAccessException("Login required for operation: " + operation);
        }
    }

    private void ensureBuildingHasFloor() {
        if (building.getFloors().isEmpty()) {
            building.addFloor(new Floor("Ground Floor", 1));
        }
    }

    private int readInt() throws InvalidInputException {
        String value = scanner.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("number", value, "whole number");
        }
    }

    private double readDouble() throws InvalidInputException {
        String value = scanner.nextLine().trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("number", value, "decimal number");
        }
    }

    private LocalTime readTime() throws InvalidInputException {
        String value = scanner.nextLine().trim();
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            throw new InvalidInputException("time", value, "HH:mm");
        }
    }

    private boolean readYesNo() throws InvalidInputException {
        String value = scanner.nextLine().trim().toLowerCase();
        if (value.equals("yes") || value.equals("y")) {
            return true;
        }
        if (value.equals("no") || value.equals("n")) {
            return false;
        }
        throw new InvalidInputException("answer", value, "yes or no");
    }
}
