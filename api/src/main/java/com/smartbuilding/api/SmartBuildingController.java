package com.smartbuilding.api;

import com.smartbuilding.exception.EquipmentNotFoundException;
import com.smartbuilding.exception.FileOperationException;
import com.smartbuilding.exception.InvalidAccessException;
import com.smartbuilding.exception.InvalidInputException;
import com.smartbuilding.model.Alert;
import com.smartbuilding.model.Building;
import com.smartbuilding.model.Equipment;
import com.smartbuilding.model.Floor;
import com.smartbuilding.model.LightingSystem;
import com.smartbuilding.model.Room;
import com.smartbuilding.model.SecuritySystem;
import com.smartbuilding.model.User;
import com.smartbuilding.service.BuildingManager;
import com.smartbuilding.service.ReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for the original console simulation.
 * The endpoints map to the same major menu groups exposed by BuildingManager.
 */
@RestController
@RequestMapping("/api")
public class SmartBuildingController {

    private final BuildingManager buildingManager;

    @Autowired
    public SmartBuildingController(BuildingManager buildingManager) {
        this.buildingManager = buildingManager;
    }

    @GetMapping("/session")
    public SessionDto getSession() {
        User user = buildingManager.getCurrentUser();
        return user == null
                ? new SessionDto(false, null, null)
                : new SessionDto(true, user.getUsername(), user.getRole());
    }

    @PostMapping("/login")
    public SessionDto login(@RequestBody LoginRequest req) throws InvalidAccessException {
        requireText(req.username, "Username");
        requireText(req.password, "Password");
        buildingManager.login(req.username, req.password);
        return getSession();
    }

    @PostMapping("/logout")
    public ApiMessage logout() {
        buildingManager.logout();
        return new ApiMessage("Logged out");
    }

    @GetMapping("/building")
    public BuildingDto getBuilding() {
        return toBuildingDto(buildingManager.getBuilding());
    }

    @PutMapping("/building")
    public BuildingDto updateBuilding(@RequestBody BuildingUpdateDto dto) throws InvalidAccessException {
        buildingManager.updateBuildingDetails(dto.name, dto.address);
        return getBuilding();
    }

    @GetMapping("/floors")
    public List<FloorDto> getAllFloors() {
        return buildingManager.getBuilding().getFloors().stream()
                .map(this::toFloorDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/floors")
    public FloorDto addFloor(@RequestBody FloorCreateDto dto) throws InvalidAccessException {
        buildingManager.addFloor(dto.name, dto.number);
        return toFloorDto(requireFloor(dto.number));
    }

    @PutMapping("/floors/{floorNumber}")
    public FloorDto updateFloor(@PathVariable int floorNumber, @RequestBody UpdateFloorDto dto)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.updateFloor(floorNumber, dto.name);
        return toFloorDto(requireFloor(floorNumber));
    }

    @DeleteMapping("/floors/{floorNumber}")
    public ApiMessage deleteFloor(@PathVariable int floorNumber)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.deleteFloor(floorNumber);
        return new ApiMessage("Floor deleted");
    }

    @GetMapping("/rooms")
    public List<RoomDto> getRooms() {
        List<RoomDto> rooms = new ArrayList<>();
        for (Floor floor : buildingManager.getBuilding().getFloors()) {
            for (Room room : floor.getRooms()) {
                rooms.add(toRoomDto(floor.getFloorNumber(), room));
            }
        }
        return rooms;
    }

    @PostMapping("/rooms")
    public RoomDto addRoom(@RequestBody RoomCreateDto dto)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.addRoom(dto.floorNumber, dto.name, dto.location, dto.capacity);
        Room created = latestRoomOnFloor(dto.floorNumber);
        return toRoomDto(dto.floorNumber, created);
    }

    @PutMapping("/rooms/{roomId}")
    public RoomDto updateRoom(@PathVariable String roomId, @RequestBody RoomUpdateDto dto)
            throws InvalidAccessException, EquipmentNotFoundException {
        buildingManager.updateRoom(roomId, dto.name, dto.location, dto.capacity);
        RoomLocation room = requireRoomWithFloor(roomId);
        return toRoomDto(room.floorNumber, room.room);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ApiMessage deleteRoom(@PathVariable String roomId)
            throws InvalidAccessException, EquipmentNotFoundException {
        buildingManager.deleteRoom(roomId);
        return new ApiMessage("Room deleted");
    }

    @GetMapping("/equipment")
    public List<EquipmentDto> getEquipment() {
        List<EquipmentDto> equipment = new ArrayList<>();
        for (Floor floor : buildingManager.getBuilding().getFloors()) {
            for (Room room : floor.getRooms()) {
                for (Equipment item : room.getEquipmentList()) {
                    equipment.add(toEquipmentDto(room.getComponentId(), room.getName(), item));
                }
            }
        }
        return equipment;
    }

    @GetMapping("/hvac")
    public List<EquipmentDto> getHvacEquipment() {
        return getEquipment().stream()
                .filter(item -> "HVAC".equalsIgnoreCase(item.type))
                .collect(Collectors.toList());
    }

    @PostMapping("/equipment")
    public EquipmentDto addEquipment(@RequestBody EquipmentCreateDto dto)
            throws InvalidAccessException, EquipmentNotFoundException {
        buildingManager.addEquipment(dto.roomId, dto.name, dto.type, dto.energyConsumption);
        Room room = requireRoomWithFloor(dto.roomId).room;
        Equipment created = room.getEquipmentList().get(room.getEquipmentList().size() - 1);
        return toEquipmentDto(room.getComponentId(), room.getName(), created);
    }

    @PutMapping("/equipment/{equipmentId}")
    public EquipmentDto updateEquipment(@PathVariable String equipmentId, @RequestBody EquipmentUpdateDto dto)
            throws InvalidAccessException, InvalidInputException, EquipmentNotFoundException {
        buildingManager.updateEquipment(equipmentId, dto.name, dto.location, dto.type,
                dto.energyConsumption, dto.status);
        EquipmentLocation location = requireEquipmentWithRoom(equipmentId);
        return toEquipmentDto(location.room.getComponentId(), location.room.getName(), location.equipment);
    }

    @DeleteMapping("/equipment/{equipmentId}")
    public ApiMessage deleteEquipment(@PathVariable String equipmentId)
            throws InvalidAccessException, EquipmentNotFoundException {
        buildingManager.deleteEquipment(equipmentId);
        return new ApiMessage("Equipment deleted");
    }

    @PostMapping("/occupancy/{roomId}")
    public RoomDto setOccupancy(@PathVariable String roomId, @RequestBody OccupancyUpdate dto)
            throws InvalidAccessException, InvalidInputException, EquipmentNotFoundException {
        buildingManager.updateOccupancy(roomId, dto.count);
        RoomLocation room = requireRoomWithFloor(roomId);
        return toRoomDto(room.floorNumber, room.room);
    }

    @PostMapping("/occupancy/{roomId}/increment")
    public RoomDto incrementOccupancy(@PathVariable String roomId)
            throws InvalidAccessException, InvalidInputException, EquipmentNotFoundException {
        ensureOccupancyRole("Increase Occupancy");
        RoomLocation room = requireRoomWithFloor(roomId);
        room.room.incrementOccupancy();
        return toRoomDto(room.floorNumber, room.room);
    }

    @PostMapping("/occupancy/{roomId}/decrement")
    public RoomDto decrementOccupancy(@PathVariable String roomId)
            throws InvalidAccessException, InvalidInputException, EquipmentNotFoundException {
        ensureOccupancyRole("Decrease Occupancy");
        RoomLocation room = requireRoomWithFloor(roomId);
        room.room.decrementOccupancy();
        return toRoomDto(room.floorNumber, room.room);
    }

    @GetMapping("/lighting")
    public List<LightDto> getAllLights() {
        return buildingManager.getBuilding().getLightingSystem().getLights().stream()
                .map(this::toLightDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/lights")
    public LightDto addLight(@RequestBody LightCreateDto dto) throws InvalidAccessException {
        buildingManager.addLight(dto.location);
        List<LightingSystem.Light> lights = buildingManager.getBuilding().getLightingSystem().getLights();
        return toLightDto(lights.get(lights.size() - 1));
    }

    @PutMapping("/lights/{lightId}")
    public LightDto updateLight(@PathVariable String lightId, @RequestBody LightUpdateDto dto)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.updateLight(lightId, dto.location, dto.brightness);
        return toLightDto(requireLight(lightId));
    }

    @DeleteMapping("/lights/{lightId}")
    public ApiMessage deleteLight(@PathVariable String lightId)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.deleteLight(lightId);
        return new ApiMessage("Light deleted");
    }

    @PostMapping("/lights/{lightId}/on")
    public LightDto turnLightOn(@PathVariable String lightId)
            throws InvalidAccessException, InvalidInputException {
        ensureLightingRole("Turn Light On");
        LightingSystem.Light light = requireLight(lightId);
        light.turnOn();
        return toLightDto(light);
    }

    @PostMapping("/lights/{lightId}/off")
    public LightDto turnLightOff(@PathVariable String lightId)
            throws InvalidAccessException, InvalidInputException {
        ensureLightingRole("Turn Light Off");
        LightingSystem.Light light = requireLight(lightId);
        light.turnOff();
        return toLightDto(light);
    }

    @PostMapping("/lights/all")
    public List<LightDto> controlAllLights(@RequestBody LightAllDto dto) throws InvalidAccessException {
        ensureLightingRole("Turn All Lights");
        buildingManager.getBuilding().getLightingSystem().controlAllLights(dto.on);
        return getAllLights();
    }

    @PostMapping("/lighting/schedule")
    public ApiMessage setLightingSchedule(@RequestBody LightingScheduleDto dto)
            throws InvalidAccessException {
        ensureLightingRole("Set Lighting Schedule");
        buildingManager.getBuilding().getLightingSystem().setSchedule(
                java.time.LocalTime.parse(dto.onTime),
                java.time.LocalTime.parse(dto.offTime));
        return new ApiMessage("Lighting schedule updated");
    }

    @PostMapping("/lighting/auto")
    public List<LightDto> runAutomaticLighting() throws InvalidAccessException {
        ensureLightingRole("Run Automatic Lighting");
        buildingManager.getBuilding().getLightingSystem().autoControlBasedOnTime();
        return getAllLights();
    }

    @GetMapping("/security")
    public SecurityDto getSecurity() {
        SecuritySystem security = buildingManager.getBuilding().getSecuritySystem();
        return new SecurityDto(security.isSurveillanceActive(),
                security.getAccessLogs().stream().map(this::toAccessLogDto).collect(Collectors.toList()),
                security.getIncidents().stream().map(this::toIncidentDto).collect(Collectors.toList()),
                security.getAlarms().stream().map(this::toAlarmDto).collect(Collectors.toList()));
    }

    @PostMapping("/security/access-logs")
    public AccessLogDto addAccessLog(@RequestBody AccessLogCreateDto dto) throws InvalidAccessException {
        ensureSecurityRole("Add Access Log");
        SecuritySystem security = buildingManager.getBuilding().getSecuritySystem();
        security.logAccess(dto.userId, dto.location, dto.accessType, dto.authorized);
        List<SecuritySystem.AccessLog> logs = security.getAccessLogs();
        return toAccessLogDto(logs.get(logs.size() - 1));
    }

    @DeleteMapping("/security/access-logs/{logId}")
    public ApiMessage deleteAccessLog(@PathVariable String logId)
            throws InvalidAccessException, InvalidInputException {
        ensureSecurityRole("Delete Access Log");
        if (!buildingManager.getBuilding().getSecuritySystem().deleteAccessLog(logId)) {
            throw new InvalidInputException("accessLog", logId, "existing access log");
        }
        return new ApiMessage("Access log deleted");
    }

    @PostMapping("/security/incidents")
    public IncidentDto addIncident(@RequestBody IncidentCreateDto dto) throws InvalidAccessException {
        ensureSecurityRole("Add Incident");
        SecuritySystem security = buildingManager.getBuilding().getSecuritySystem();
        security.reportIncident(dto.type, dto.description, dto.severity);
        List<SecuritySystem.Incident> incidents = security.getIncidents();
        return toIncidentDto(incidents.get(incidents.size() - 1));
    }

    @PutMapping("/security/incidents/{incidentId}")
    public IncidentDto updateIncident(@PathVariable String incidentId, @RequestBody IncidentUpdateDto dto)
            throws InvalidAccessException, InvalidInputException {
        ensureSecurityRole("Update Incident");
        SecuritySystem.Incident incident = requireIncident(incidentId);
        incident.updateStatus(dto.status);
        return toIncidentDto(incident);
    }

    @DeleteMapping("/security/incidents/{incidentId}")
    public ApiMessage deleteIncident(@PathVariable String incidentId)
            throws InvalidAccessException, InvalidInputException {
        ensureSecurityRole("Delete Incident");
        if (!buildingManager.getBuilding().getSecuritySystem().deleteIncident(incidentId)) {
            throw new InvalidInputException("incident", incidentId, "existing incident");
        }
        return new ApiMessage("Incident deleted");
    }

    @PostMapping("/security/alarms")
    public AlarmDto triggerAlarm(@RequestBody AlarmCreateDto dto) throws InvalidAccessException {
        ensureSecurityRole("Trigger Alarm");
        SecuritySystem security = buildingManager.getBuilding().getSecuritySystem();
        security.triggerAlarm(dto.type, dto.location);
        List<SecuritySystem.Alarm> alarms = security.getAlarms();
        return toAlarmDto(alarms.get(alarms.size() - 1));
    }

    @PostMapping("/security/alarms/{alarmId}/deactivate")
    public AlarmDto deactivateAlarm(@PathVariable String alarmId)
            throws InvalidAccessException, InvalidInputException {
        ensureSecurityRole("Deactivate Alarm");
        if (!buildingManager.getBuilding().getSecuritySystem().deactivateAlarm(alarmId)) {
            throw new InvalidInputException("alarm", alarmId, "existing alarm");
        }
        return toAlarmDto(requireAlarm(alarmId));
    }

    @DeleteMapping("/security/alarms/{alarmId}")
    public ApiMessage deleteAlarm(@PathVariable String alarmId)
            throws InvalidAccessException, InvalidInputException {
        ensureSecurityRole("Delete Alarm");
        if (!buildingManager.getBuilding().getSecuritySystem().deleteAlarm(alarmId)) {
            throw new InvalidInputException("alarm", alarmId, "existing alarm");
        }
        return new ApiMessage("Alarm deleted");
    }

    @PostMapping("/security/surveillance")
    public SecurityDto setSurveillance(@RequestBody SurveillanceDto dto) throws InvalidAccessException {
        ensureSecurityRole("Toggle Surveillance");
        SecuritySystem security = buildingManager.getBuilding().getSecuritySystem();
        security.setSurveillanceActive(dto.active);
        return getSecurity();
    }

    @GetMapping("/alerts")
    public AlertListsDto getAlerts() {
        return new AlertListsDto(
                buildingManager.getAlertSystem().getActiveAlerts().stream()
                        .map(alert -> toAlertDto(alert, false)).collect(Collectors.toList()),
                buildingManager.getAlertSystem().getResolvedAlerts().stream()
                        .map(alert -> toAlertDto(alert, true)).collect(Collectors.toList()));
    }

    @PostMapping("/alerts")
    public AlertDto createAlert(@RequestBody AlertCreateDto dto) throws InvalidAccessException {
        buildingManager.triggerAlert(dto.type, dto.message, dto.severity);
        List<Alert> alerts = buildingManager.getAlertSystem().getActiveAlerts();
        return toAlertDto(alerts.get(alerts.size() - 1), false);
    }

    @PutMapping("/alerts/{alertId}")
    public AlertDto updateAlert(@PathVariable String alertId, @RequestBody AlertUpdateDto dto)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.updateAlert(alertId, dto.message, dto.severity);
        return toAlertDto(requireActiveAlert(alertId), false);
    }

    @PostMapping("/alerts/{alertId}/ack")
    public AlertDto acknowledgeAlert(@PathVariable String alertId)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.acknowledgeAlert(alertId);
        return toAlertDto(requireActiveAlert(alertId), false);
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public AlertListsDto resolveAlert(@PathVariable String alertId)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.resolveAlert(alertId);
        return getAlerts();
    }

    @DeleteMapping("/alerts/{alertId}")
    public ApiMessage deleteAlert(@PathVariable String alertId)
            throws InvalidAccessException, InvalidInputException {
        buildingManager.deleteAlert(alertId);
        return new ApiMessage("Alert deleted");
    }

    @GetMapping("/reports/{type}")
    public ReportDto getReport(@PathVariable String type) {
        return new ReportDto(type.toUpperCase(), reportGenerator().generateCombinedReport(type));
    }

    @GetMapping("/reports/combined")
    public ReportDto getCombinedReport() {
        return new ReportDto("COMBINED", reportGenerator().generateCombinedReport(
                "ENERGY", "OCCUPANCY", "EQUIPMENT", "SECURITY", "LIGHTING"));
    }

    @PostMapping("/reports/export")
    public ApiMessage exportReport(@RequestBody ReportRequestDto dto)
            throws FileOperationException {
        buildingManager.generateAndExportReport(dto.type, true);
        return new ApiMessage("Report exported to the data folder");
    }

    @GetMapping("/status")
    public CompleteStatusDto getCompleteStatus() {
        Building building = buildingManager.getBuilding();
        return new CompleteStatusDto(toBuildingDto(building), building.toString(), building.getTotalEnergyConsumption());
    }

    @PostMapping("/save")
    public ApiMessage saveData() throws InvalidAccessException, FileOperationException {
        buildingManager.saveData();
        return new ApiMessage("System data saved");
    }

    @PostMapping("/load")
    public ApiMessage loadData() throws InvalidAccessException, FileOperationException {
        buildingManager.loadData();
        return new ApiMessage("System data loaded");
    }

    private BuildingDto toBuildingDto(Building building) {
        return new BuildingDto(building.getBuildingName(), building.getAddress(),
                building.getFloors().size(), building.getTotalCapacity(),
                building.getTotalOccupancy(), building.getTotalEnergyConsumption());
    }

    private FloorDto toFloorDto(Floor floor) {
        int totalCapacity = floor.getRooms().stream().mapToInt(Room::getCapacity).sum();
        int currentOccupancy = floor.getRooms().stream().mapToInt(Room::getCurrentOccupancy).sum();
        double occupancyRate = totalCapacity == 0 ? 0.0 : currentOccupancy * 100.0 / totalCapacity;
        return new FloorDto(floor.getComponentId(), floor.getFloorNumber(), floor.getName(),
                totalCapacity, currentOccupancy, occupancyRate, floor.getRoomCount());
    }

    private RoomDto toRoomDto(int floorNumber, Room room) {
        return new RoomDto(room.getComponentId(), floorNumber, room.getName(), room.getLocation(),
                room.getCapacity(), room.getCurrentOccupancy(), room.getOccupancyRate(),
                room.getEquipmentCount());
    }

    private EquipmentDto toEquipmentDto(String roomId, String roomName, Equipment equipment) {
        return new EquipmentDto(equipment.getComponentId(), roomId, roomName, equipment.getName(),
                equipment.getLocation(), equipment.getEquipmentType(), equipment.getEnergyConsumption(),
                equipment.getStatus());
    }

    private LightDto toLightDto(LightingSystem.Light light) {
        return new LightDto(light.getLightId(), light.getLocation(), light.isOn(), light.getBrightnessLevel());
    }

    private AccessLogDto toAccessLogDto(SecuritySystem.AccessLog log) {
        return new AccessLogDto(log.getLogId(), log.getUserId(), log.getLocation(),
                String.valueOf(log.getAccessTime()), log.getAccessType(), log.isAuthorized());
    }

    private IncidentDto toIncidentDto(SecuritySystem.Incident incident) {
        return new IncidentDto(incident.getIncidentId(), incident.getType(), incident.getDescription(),
                String.valueOf(incident.getReportedTime()), incident.getStatus(), incident.getSeverity());
    }

    private AlarmDto toAlarmDto(SecuritySystem.Alarm alarm) {
        return new AlarmDto(alarm.getAlarmId(), alarm.getAlarmType(), alarm.getLocation(),
                String.valueOf(alarm.getTriggeredTime()), alarm.isActive());
    }

    private AlertDto toAlertDto(Alert alert, boolean resolved) {
        return new AlertDto(alert.getAlertId(), alert.getAlertType(), alert.getMessage(),
                alert.getSeverity(), alert.isAcknowledged(), alert.getAssignedTo(),
                String.valueOf(alert.getTimestamp()), resolved);
    }

    private Floor requireFloor(int floorNumber) {
        Floor floor = buildingManager.getBuilding().getFloorByNumber(floorNumber);
        if (floor == null) {
            throw new IllegalArgumentException("Floor not found: " + floorNumber);
        }
        return floor;
    }

    private Room latestRoomOnFloor(int floorNumber) {
        Floor floor = requireFloor(floorNumber);
        List<Room> rooms = floor.getRooms();
        if (rooms.isEmpty()) {
            throw new IllegalStateException("Room was not created");
        }
        return rooms.get(rooms.size() - 1);
    }

    private RoomLocation requireRoomWithFloor(String roomId) throws EquipmentNotFoundException {
        for (Floor floor : buildingManager.getBuilding().getFloors()) {
            Room room = floor.getRoomById(roomId);
            if (room != null) {
                return new RoomLocation(floor.getFloorNumber(), room);
            }
        }
        throw new EquipmentNotFoundException(roomId);
    }

    private EquipmentLocation requireEquipmentWithRoom(String equipmentId) throws EquipmentNotFoundException {
        for (Floor floor : buildingManager.getBuilding().getFloors()) {
            for (Room room : floor.getRooms()) {
                Equipment equipment = room.getEquipmentById(equipmentId);
                if (equipment != null) {
                    return new EquipmentLocation(room, equipment);
                }
            }
        }
        throw new EquipmentNotFoundException(equipmentId);
    }

    private LightingSystem.Light requireLight(String lightId) throws InvalidInputException {
        LightingSystem.Light light = buildingManager.getBuilding().getLightingSystem().getLightById(lightId);
        if (light == null) {
            throw new InvalidInputException("lightId", lightId, "existing light ID");
        }
        return light;
    }

    private SecuritySystem.Incident requireIncident(String incidentId) throws InvalidInputException {
        SecuritySystem.Incident incident = buildingManager.getBuilding().getSecuritySystem().getIncidentById(incidentId);
        if (incident == null) {
            throw new InvalidInputException("incident", incidentId, "existing incident");
        }
        return incident;
    }

    private SecuritySystem.Alarm requireAlarm(String alarmId) throws InvalidInputException {
        SecuritySystem.Alarm alarm = buildingManager.getBuilding().getSecuritySystem().getAlarmById(alarmId);
        if (alarm == null) {
            throw new InvalidInputException("alarm", alarmId, "existing alarm");
        }
        return alarm;
    }

    private Alert requireActiveAlert(String alertId) throws InvalidInputException {
        Alert alert = buildingManager.getAlertSystem().getActiveAlertById(alertId);
        if (alert == null) {
            throw new InvalidInputException("alertId", alertId, "active alert ID");
        }
        return alert;
    }

    private void ensureOccupancyRole(String operation) throws InvalidAccessException {
        ensureRole(operation, "ADMINISTRATOR", "MAINTENANCE", "SECURITY");
    }

    private void ensureLightingRole(String operation) throws InvalidAccessException {
        ensureRole(operation, "ADMINISTRATOR", "MAINTENANCE");
    }

    private void ensureSecurityRole(String operation) throws InvalidAccessException {
        ensureRole(operation, "ADMINISTRATOR", "SECURITY");
    }

    private void ensureRole(String operation, String... roles) throws InvalidAccessException {
        User user = buildingManager.getCurrentUser();
        if (user == null) {
            throw new InvalidAccessException("Login required for operation: " + operation);
        }
        for (String role : roles) {
            if (role.equals(user.getRole())) {
                return;
            }
        }
        throw new InvalidAccessException(user.getUsername(), operation);
    }

    private ReportGenerator reportGenerator() {
        Building building = buildingManager.getBuilding();
        return new ReportGenerator(building, building.getSecuritySystem(),
                building.getOccupancyMonitor(), building.getLightingSystem());
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    @ExceptionHandler(InvalidAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccess(InvalidAccessException ex) {
        return error("ACCESS_ERROR", ex.getMessage());
    }

    @ExceptionHandler({InvalidInputException.class, EquipmentNotFoundException.class,
            IllegalArgumentException.class, FileOperationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(Exception ex) {
        return error("BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnexpected(Exception ex) {
        return error("SERVER_ERROR", ex.getMessage());
    }

    private Map<String, String> error(String code, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message == null ? "Unexpected error" : message);
        return body;
    }

    static class RoomLocation {
        final int floorNumber;
        final Room room;
        RoomLocation(int floorNumber, Room room) {
            this.floorNumber = floorNumber;
            this.room = room;
        }
    }

    static class EquipmentLocation {
        final Room room;
        final Equipment equipment;
        EquipmentLocation(Room room, Equipment equipment) {
            this.room = room;
            this.equipment = equipment;
        }
    }

    static class LoginRequest { public String username; public String password; }
    static class SessionDto {
        public boolean loggedIn; public String username; public String role;
        SessionDto(boolean loggedIn, String username, String role) {
            this.loggedIn = loggedIn; this.username = username; this.role = role;
        }
    }
    static class ApiMessage {
        public String message;
        ApiMessage(String message) { this.message = message; }
    }
    static class BuildingDto {
        public String name; public String address; public int floorCount; public int totalCapacity;
        public int totalOccupancy; public double totalEnergy;
        BuildingDto(String name, String address, int floorCount, int totalCapacity,
                    int totalOccupancy, double totalEnergy) {
            this.name = name; this.address = address; this.floorCount = floorCount;
            this.totalCapacity = totalCapacity; this.totalOccupancy = totalOccupancy;
            this.totalEnergy = totalEnergy;
        }
    }
    static class BuildingUpdateDto { public String name; public String address; }
    static class FloorDto {
        public String componentId; public int floorNumber; public String floorName; public int totalCapacity;
        public int currentOccupancy; public double occupancyRate; public int roomCount;
        FloorDto(String componentId, int floorNumber, String floorName, int totalCapacity,
                 int currentOccupancy, double occupancyRate, int roomCount) {
            this.componentId = componentId; this.floorNumber = floorNumber; this.floorName = floorName;
            this.totalCapacity = totalCapacity; this.currentOccupancy = currentOccupancy;
            this.occupancyRate = occupancyRate; this.roomCount = roomCount;
        }
    }
    static class FloorCreateDto { public String name; public int number; }
    static class UpdateFloorDto { public String name; }
    static class RoomCreateDto { public int floorNumber; public String name; public int capacity; public String location; }
    static class RoomUpdateDto { public String name; public int capacity; public String location; }
    static class RoomDto {
        public String componentId; public int floorNumber; public String name; public String location;
        public int capacity; public int currentOccupancy; public double occupancyRate; public int equipmentCount;
        RoomDto(String componentId, int floorNumber, String name, String location, int capacity,
                int currentOccupancy, double occupancyRate, int equipmentCount) {
            this.componentId = componentId; this.floorNumber = floorNumber; this.name = name;
            this.location = location; this.capacity = capacity; this.currentOccupancy = currentOccupancy;
            this.occupancyRate = occupancyRate; this.equipmentCount = equipmentCount;
        }
    }
    static class EquipmentCreateDto { public String roomId; public String name; public String type; public double energyConsumption; }
    static class EquipmentUpdateDto {
        public String name; public String location; public String type; public double energyConsumption; public String status;
    }
    static class EquipmentDto {
        public String equipmentId; public String roomId; public String roomName; public String name;
        public String location; public String type; public double energyConsumption; public String status;
        EquipmentDto(String equipmentId, String roomId, String roomName, String name, String location,
                     String type, double energyConsumption, String status) {
            this.equipmentId = equipmentId; this.roomId = roomId; this.roomName = roomName;
            this.name = name; this.location = location; this.type = type;
            this.energyConsumption = energyConsumption; this.status = status;
        }
    }
    static class OccupancyUpdate { public int count; }
    static class LightCreateDto { public String location; }
    static class LightUpdateDto { public String location; public int brightness; }
    static class LightAllDto { public boolean on; }
    static class LightingScheduleDto { public String onTime; public String offTime; }
    static class LightDto {
        public String lightId; public String location; public boolean on; public int brightness;
        LightDto(String lightId, String location, boolean on, int brightness) {
            this.lightId = lightId; this.location = location; this.on = on; this.brightness = brightness;
        }
    }
    static class SecurityDto {
        public boolean surveillanceActive; public List<AccessLogDto> accessLogs;
        public List<IncidentDto> incidents; public List<AlarmDto> alarms;
        SecurityDto(boolean surveillanceActive, List<AccessLogDto> accessLogs,
                    List<IncidentDto> incidents, List<AlarmDto> alarms) {
            this.surveillanceActive = surveillanceActive; this.accessLogs = accessLogs;
            this.incidents = incidents; this.alarms = alarms;
        }
    }
    static class AccessLogCreateDto { public String userId; public String location; public String accessType; public boolean authorized; }
    static class AccessLogDto {
        public String logId; public String userId; public String location; public String accessTime;
        public String accessType; public boolean authorized;
        AccessLogDto(String logId, String userId, String location, String accessTime, String accessType, boolean authorized) {
            this.logId = logId; this.userId = userId; this.location = location;
            this.accessTime = accessTime; this.accessType = accessType; this.authorized = authorized;
        }
    }
    static class IncidentCreateDto { public String type; public String description; public String severity; }
    static class IncidentUpdateDto { public String status; }
    static class IncidentDto {
        public String incidentId; public String type; public String description; public String reportedTime;
        public String status; public String severity;
        IncidentDto(String incidentId, String type, String description, String reportedTime, String status, String severity) {
            this.incidentId = incidentId; this.type = type; this.description = description;
            this.reportedTime = reportedTime; this.status = status; this.severity = severity;
        }
    }
    static class AlarmCreateDto { public String type; public String location; }
    static class AlarmDto {
        public String alarmId; public String alarmType; public String location; public String triggeredTime; public boolean active;
        AlarmDto(String alarmId, String alarmType, String location, String triggeredTime, boolean active) {
            this.alarmId = alarmId; this.alarmType = alarmType; this.location = location;
            this.triggeredTime = triggeredTime; this.active = active;
        }
    }
    static class SurveillanceDto { public boolean active; }
    static class AlertCreateDto { public String type; public String message; public String severity; public String source; }
    static class AlertUpdateDto { public String message; public String severity; }
    static class AlertDto {
        public String alertId; public String type; public String message; public String severity;
        public boolean acknowledged; public String assignedTo; public String timestamp; public boolean resolved;
        AlertDto(String alertId, String type, String message, String severity, boolean acknowledged,
                 String assignedTo, String timestamp, boolean resolved) {
            this.alertId = alertId; this.type = type; this.message = message; this.severity = severity;
            this.acknowledged = acknowledged; this.assignedTo = assignedTo;
            this.timestamp = timestamp; this.resolved = resolved;
        }
    }
    static class AlertListsDto {
        public List<AlertDto> active; public List<AlertDto> resolved;
        AlertListsDto(List<AlertDto> active, List<AlertDto> resolved) {
            this.active = active; this.resolved = resolved;
        }
    }
    static class ReportRequestDto { public String type; }
    static class ReportDto {
        public String type; public String text;
        ReportDto(String type, String text) { this.type = type; this.text = text; }
    }
    static class CompleteStatusDto {
        public BuildingDto building; public String summary; public double totalEnergy;
        CompleteStatusDto(BuildingDto building, String summary, double totalEnergy) {
            this.building = building; this.summary = summary; this.totalEnergy = totalEnergy;
        }
    }
}
