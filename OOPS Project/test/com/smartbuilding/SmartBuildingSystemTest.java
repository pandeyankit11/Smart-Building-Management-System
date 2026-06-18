package com.smartbuilding;

import com.smartbuilding.exception.InvalidAccessException;
import com.smartbuilding.exception.InvalidInputException;
import com.smartbuilding.model.AlertListener;
import com.smartbuilding.model.Building;
import com.smartbuilding.model.Equipment;
import com.smartbuilding.model.Floor;
import com.smartbuilding.model.GeneralUser;
import com.smartbuilding.model.LightingSystem;
import com.smartbuilding.model.Room;
import com.smartbuilding.model.SecuritySystem;
import com.smartbuilding.service.AlertSystem;
import com.smartbuilding.service.BuildingManager;
import com.smartbuilding.service.ReportGenerator;
import com.smartbuilding.util.FileHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public final class SmartBuildingSystemTest {
    private static int passed;
    private static int failed;

    private SmartBuildingSystemTest() {
    }

    public static void main(String[] args) throws Exception {
        run("demo data and unique IDs", SmartBuildingSystemTest::testDemoDataAndUniqueIds);
        run("equipment totals and report accuracy", SmartBuildingSystemTest::testEquipmentReports);
        run("occupancy validation", SmartBuildingSystemTest::testOccupancyValidation);
        run("lighting controls and validation", SmartBuildingSystemTest::testLighting);
        run("security incident lifecycle", SmartBuildingSystemTest::testSecurity);
        run("alert delivery and resolution", SmartBuildingSystemTest::testAlerts);
        run("role-based access control", SmartBuildingSystemTest::testRoleBasedAccess);
        run("persistence restores full state", SmartBuildingSystemTest::testPersistence);
        run("file configuration and credentials", SmartBuildingSystemTest::testFileOperations);

        System.out.println("\nTest result: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            throw new AssertionError("Test suite failed");
        }
    }

    private static void testDemoDataAndUniqueIds() {
        BuildingManager manager = manager("demo");
        manager.initializeDemoData();
        manager.initializeDemoData();
        Building building = manager.getBuilding();

        assertEquals(2, building.getFloors().size(), "floor count");
        assertEquals(5, building.getFloors().stream().mapToInt(Floor::getRoomCount).sum(), "room count");
        assertEquals(3, building.getTotalEquipmentCount(), "equipment count");
        assertEquals(35, building.getTotalOccupancy(), "total occupancy");

        Set<String> ids = new HashSet<>();
        building.getFloors().forEach(floor -> {
            assertTrue(ids.add(floor.getComponentId()), "duplicate floor ID");
            floor.getRooms().forEach(room -> {
                assertTrue(ids.add(room.getComponentId()), "duplicate room ID");
                room.getEquipmentList().forEach(equipment ->
                        assertTrue(ids.add(equipment.getComponentId()), "duplicate equipment ID"));
            });
        });
        building.getLightingSystem().getLights().forEach(light ->
                assertTrue(ids.add(light.getLightId()), "duplicate light ID"));
    }

    private static void testEquipmentReports() throws Exception {
        BuildingManager manager = manager("reports");
        manager.initializeDemoData();
        Building building = manager.getBuilding();
        Equipment hvac = building.getFloorByNumber(1).getRooms().get(0).getEquipmentList().get(0);
        hvac.updateStatus("maintenance");

        ReportGenerator reports = new ReportGenerator(building, building.getSecuritySystem(),
                building.getOccupancyMonitor(), building.getLightingSystem());
        String report = reports.generateEquipmentReport();

        assertContains(report, "HVAC: Total=1, Operational=0, Maintenance=1, Malfunctioning=0");
        assertEquals(1, building.getTotalEquipmentCount("hvac"), "case-insensitive equipment count");
        assertContains(reports.generateEnergyReport(), "Total Energy Consumption: 2150.0 kWh");
    }

    private static void testOccupancyValidation() throws Exception {
        Room room = new Room("Test Room", 5);
        room.updateOccupancy(5);
        assertEquals(100.0, room.getOccupancyRate(), "occupancy rate");
        assertThrows(InvalidInputException.class, () -> room.updateOccupancy(6));
        assertThrows(InvalidInputException.class, room::incrementOccupancy);
        assertThrows(IllegalArgumentException.class, () -> new Room("Invalid", 0));
    }

    private static void testLighting() throws Exception {
        LightingSystem lighting = new LightingSystem("Test Lighting");
        lighting.addMultipleLights("North", "South", "East");
        assertEquals(3, lighting.getLightCount(), "light count");
        assertEquals(3L, lighting.getLights().stream().map(LightingSystem.Light::getLightId).distinct().count(),
                "unique light IDs");
        lighting.controlAllLights(true);
        assertTrue(lighting.getLights().stream().allMatch(LightingSystem.Light::isOn), "lights should be on");
        assertThrows(InvalidInputException.class, () -> lighting.getLights().get(0).setBrightness(101));
        assertThrows(IllegalArgumentException.class, () -> lighting.getLights().get(0).recordEnergyUsage(-1));
    }

    private static void testSecurity() throws Exception {
        SecuritySystem security = new SecuritySystem("Test Security");
        security.logAccess("USR1", "Lobby", "ENTRY", false);
        security.reportIncident("ACCESS", "Unauthorized entry", "HIGH");
        security.triggerAlarm("INTRUSION", "Lobby");

        String incidentId = security.getIncidents().get(0).getIncidentId();
        security.investigateIncident(incidentId);
        assertEquals("INVESTIGATING", security.getIncidents().get(0).getStatus(), "incident status");
        security.resolveIncident(incidentId);
        assertEquals("RESOLVED", security.getIncidents().get(0).getStatus(), "resolved incident status");
        assertContains(security.generateSecurityReport(), "Active Alarms: 1");
    }

    private static void testAlerts() throws Exception {
        AlertSystem alerts = new AlertSystem();
        CountingListener listener = new CountingListener();
        alerts.addListener(listener);
        alerts.createAlert("EQUIPMENT_FAILURE", "Pump failed", "HIGH");
        alerts.notifyListeners();
        alerts.notifyListeners();
        assertEquals(1, listener.notifications, "alert should only be delivered once");

        String alertId = alerts.getActiveAlerts().get(0).getAlertId();
        alerts.resolveAlert(alertId);
        assertEquals(0, alerts.getActiveAlertCount(), "active alert count");
        assertEquals(1, alerts.getResolvedAlertCount(), "resolved alert count");
    }

    private static void testRoleBasedAccess() throws Exception {
        BuildingManager manager = manager("roles");
        manager.initializeDemoData();
        manager.login("user", "user123");
        assertTrue(manager.getCurrentUser().isLoggedIn(), "manager login state");
        assertThrows(InvalidAccessException.class, () -> manager.addRoom("Forbidden", 10));
        assertThrows(InvalidAccessException.class,
                () -> manager.addEquipment(firstRoomId(manager), "Device", "SENSOR", 1));

        manager.logout();
        manager.login("admin", "admin123");
        int before = manager.getBuilding().getFloorByNumber(1).getRoomCount();
        manager.addRoom("Admin Room", 10);
        assertEquals(before + 1, manager.getBuilding().getFloorByNumber(1).getRoomCount(),
                "admin room addition");
    }

    private static void testPersistence() throws Exception {
        Path directory = Files.createTempDirectory("sbms-persistence-");
        BuildingManager manager = new BuildingManager("Persistent Building", "Test Address",
                directory.toString(), new Scanner(""));
        manager.initializeDemoData();
        manager.login("admin", "admin123");
        manager.triggerAlert("SYSTEM_ERROR", "Persistence check", "WARNING");
        manager.saveData();

        manager.getBuilding().addFloor(new Floor("Temporary Floor", 99));
        manager.loadData();

        assertEquals(2, manager.getBuilding().getFloors().size(), "restored floor count");
        assertEquals(1, manager.getAlertSystem().getActiveAlertCount(), "restored alert count");
        assertEquals(null, manager.getCurrentUser(), "load should require a new login");
        manager.login("security", "sec123");
        manager.triggerAlert("SECURITY_BREACH", "Listener restored safely", "CRITICAL");
        assertEquals(2, manager.getAlertSystem().getActiveAlertCount(), "alerts work after reload");
    }

    private static void testFileOperations() throws Exception {
        Path directory = Files.createTempDirectory("sbms-files-");
        FileHandler files = new FileHandler(directory.toString());
        Path config = directory.resolve("building.conf");
        Files.writeString(config, "mode=automatic\nthreshold=80\n");
        assertContains(files.importConfiguration("building.conf"), "threshold=80");

        GeneralUser user = new GeneralUser("reader", "secret", "A-1");
        files.saveUserCredentials(user, "secret");
        assertTrue(files.validateUserCredentials("reader", "secret"), "valid credentials");
        assertFalse(files.validateUserCredentials("reader", "wrong"), "invalid credentials");
        String credentialFile = Files.readString(directory.resolve("users").resolve(user.getUserId() + ".txt"));
        assertFalse(credentialFile.contains("Password: secret"), "password must not be stored in plaintext");

        files.exportReportToFile("test report", "system");
        assertTrue(Files.list(directory).anyMatch(path -> path.getFileName().toString().startsWith("system_report_")),
                "report file should exist");
    }

    private static BuildingManager manager(String name) {
        try {
            return new BuildingManager("Test " + name, "Test Address",
                    Files.createTempDirectory("sbms-" + name + "-").toString(), new Scanner(""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String firstRoomId(BuildingManager manager) {
        return manager.getBuilding().getFloorByNumber(1).getRooms().get(0).getComponentId();
    }

    private static void run(String name, CheckedRunnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.err.println("[FAIL] " + name + ": " + error.getMessage());
            error.printStackTrace(System.err);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertContains(String actual, String expected) {
        assertTrue(actual.contains(expected), "Expected text not found: " + expected + "\nActual:\n" + actual);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expected, CheckedRunnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                return;
            }
            throw new AssertionError("Expected " + expected.getSimpleName() + " but got "
                    + error.getClass().getSimpleName(), error);
        }
        throw new AssertionError("Expected " + expected.getSimpleName() + " to be thrown");
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private static final class CountingListener implements AlertListener {
        private static final long serialVersionUID = 1L;
        private int notifications;

        @Override
        public void receiveAlert(String alertType, String message, String severity) {
            notifications++;
        }

        @Override
        public boolean canHandleAlert(String alertType) {
            return true;
        }

        @Override
        public void acknowledgeAlert(String alertId) {
        }
    }
}
