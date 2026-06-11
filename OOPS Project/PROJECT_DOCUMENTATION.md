# Smart Building Management System - Project Documentation

## 1. Project Overview

The **Smart Building Management System (SBMS)** is a comprehensive Java application designed to manage infrastructure, security, lighting, occupancy monitoring, and alert systems for residential buildings. The system includes disaster alert capabilities and provides role-based access control for administrators, maintenance staff, security personnel, and general users.

### Key Features:
- Building infrastructure management (floors, rooms, equipment)
- Outdoor lighting control with scheduling
- Security system with access logs, surveillance, alarms, and incident reports
- Real-time occupancy monitoring and analytics
- Multi-level alert and notification system
- Comprehensive reporting and analytics
- File-based data persistence
- Interactive console-based user interface

## 2. System Architecture

### 2.1 Package Structure
```
com.smartbuilding
├── SmartBuildingApp.java          (Main application)
├── model/                         (Entity classes)
│   ├── BuildingComponent.java    (Abstract base)
│   ├── User.java                 (Base user class)
│   ├── Administrator.java        (Hierarchical inheritance)
│   ├── MaintenanceStaff.java     (Hierarchical inheritance)
│   ├── SecurityStaff.java        (Hierarchical inheritance)
│   ├── GeneralUser.java          (Hierarchical inheritance)
│   ├── Building.java             (Aggregates all components)
│   ├── Floor.java
│   ├── Room.java
│   ├── Equipment.java            (Includes nested static class)
│   ├── LightingSystem.java      (Includes nested non-static class)
│   ├── SecuritySystem.java      (Includes nested static classes)
│   ├── OccupancyMonitor.java    ( Includes nested class)
│   └── Alert.java                (Includes nested enum)
├── service/
│   ├── AlertSystem.java          (Manages alerts, listeners)
│   ├── ReportGenerator.java      (Generates various reports)
│   └── BuildingManager.java      (Main service coordinator)
├── exception/
│   ├── InvalidAccessException.java
│   ├── EquipmentNotFoundException.java
│   ├── InvalidInputException.java
│   └── FileOperationException.java
└── util/
    └── FileHandler.java          (I/O operations)
```

### 2.2 UML Class Diagram (Description)

The system follows a layered architecture with clear separation of concerns:

**Inheritance Hierarchy:**
```
BuildingComponent (abstract)
├── Floor
├── Room
├── Equipment
├── LightingSystem
├── SecuritySystem
├── OccupancyMonitor
└── Building (also aggregates these)

User (base)
├── Administrator (implements AlertListener)
├── MaintenanceStaff (implements AlertListener)
├── SecurityStaff (implements AlertListener)
└── GeneralUser (implements AlertListener)
```

**Key Relationships:**
- **Aggregation:** Building contains multiple Floors, which contain multiple Rooms, which contain multiple Equipment.
- **Composition:** LightingSystem contains multiple Light objects (nested class).
- **Association:** AlertSystem maintains a list of AlertListener objects (Users).
- **Implementation:** Administrator, MaintenanceStaff, SecurityStaff, GeneralUser all implement AlertListener interface.

**Nested Classes:**
1. `Equipment.EquipmentSpecs` (static nested)
2. `LightingSystem.Light` (non-static inner)
3. `SecuritySystem.AccessLog` (static nested)
4. `SecuritySystem.Incident` (static nested)
5. `SecuritySystem.Alarm` (static nested)
6. `OccupancyMonitor.OccupancyRecord` (non-static inner)
7. `Alert.AlertTypeEnum` (nested enum)

**Interfaces:**
- `AlertListener`: Defines contract for receiving alerts.

**Abstract Class:**
- `BuildingComponent`: Provides common properties and abstract `performMaintenance()` method.

## 3. OOPS Principles & Requirements Mapping

### 3.1 Requirements Satisfied

| # | Requirement | Implementation Details | Count |
|---|-------------|------------------------|-------|
| 1 | **At least 4 classes** | Building, Room, Floor, Equipment, LightingSystem, SecuritySystem, OccupancyMonitor, User, Administrator, MaintenanceStaff, SecurityStaff, GeneralUser, etc. | 13+ |
| 2 | **Nested classes** | Equipment.EquipmentSpecs, LightingSystem.Light, SecuritySystem.AccessLog/Incident/Alarm, OccupancyMonitor.OccupancyRecord, Alert.AlertTypeEnum | 7 |
| 3 | **Abstract class** | BuildingComponent (abstract) with abstract performMaintenance() method | 1 |
| 4 | **Interface** | AlertListener with receiveAlert(), canHandleAlert(), acknowledgeAlert() | 1 |
| 5 | **Hierarchical inheritance** | User → Administrator/MaintenanceStaff/SecurityStaff/GeneralUser | 1 hierarchy (4 subclasses) |
| 6 | **Package** | com.smartbuilding (with subpackages model, service, exception, util) | 1 package structure |
| 7 | **Exception handling** | InvalidAccessException, EquipmentNotFoundException, InvalidInputException, FileOperationException | 4+ cases |
| 8 | **I/O** | FileHandler with file read/write, serialization, Scanner for input | Multiple |
| 9 | **Overloaded methods** | addEquipment(), generateReport(), sendAlert(), logAccess(), etc. | 10+ |
|10 | **Overloaded constructors** | Room(3), Equipment(4), User(3), GeneralUser(3), Administrator(2), etc. | 15+ |
|11 | **Vararg overloading** | addMultipleEquipments(String...), createAlerts(Alert...), generateCombinedReport(String...), addMultipleLights(String...) | 4 |
|12 | **Wrappers** | Integer (auto-boxing), Double, Boolean, ArrayList, HashMap used extensively | Throughout |

### 3.2 Additional OOPS Features

- **Polymorphism:** Method overriding (performMaintenance, toString), interface implementation.
- **Encapsulation:** Private fields with getters/setters, data hiding.
- **Abstraction:** Abstract classes and interfaces hide implementation details.
- **Composition:** Building aggregates multiple components.
- **Dynamic Binding:** Runtime method dispatch.

## 4. Feature Implementation Details

### 4.1 Building Infrastructure Management
- `Building` class maintains list of `Floors`.
- `Floor` contains list of `Rooms`.
- `Room` contains list of `Equipment`.
- Each component extends `BuildingComponent` with common properties.
- CRUD operations: addFloor, addRoom, addEquipment, remove methods.
- Maintenance tracking with history.

### 4.2 Lighting Management (Outdoor)
- `LightingSystem` manages multiple `Light` objects (nested class).
- Features: on/off control, brightness adjustment (0-100%).
- Automated time-based scheduling (on/off times).
- Energy consumption monitoring per light and total.

### 4.3 Security System Management
- `SecuritySystem` contains nested classes: AccessLog, Incident, Alarm.
- Features:
  - Entry/exit access logging with authorization status.
  - Incident reporting with severity levels.
  - Alarm triggering and management.
  - Surveillance monitoring simulation.
- Unauthorized access detection.
- Security reports generation.

### 4.4 Occupancy Monitoring
- `OccupancyMonitor` tracks occupancy via `OccupancyRecord` (nested class).
- Features:
  - Real-time occupancy recording per room/zone.
  - Peak occupancy period analysis.
  - Capacity validation and overcrowding alerts.
  - Occupancy utilization percentage calculation.
  - Optimization recommendations for utilities.

### 4.5 Alerts and Notifications
- `AlertSystem` manages all system alerts.
- `AlertListener` interface implemented by all User types.
- Alert types:
  - Equipment failure
  - Energy overuse
  - Security breach
  - Maintenance reminders
- Severity levels: INFO, WARNING, HIGH, CRITICAL.
- Listeners receive alerts based on their capability (canHandleAlert).
- Alert acknowledgment tracking.

### 4.6 Reporting and Analytics
- `ReportGenerator` produces:
  - Energy consumption reports (equipment + lighting)
  - Occupancy utilization reports
  - Equipment performance and status reports
  - Security incident and alarm reports
  - Combined multi-report generation (varargs)
- Reports can be viewed on-screen or exported to files.

### 4.7 Role-Based Access Control
Four user roles with different permissions:
1. **Administrator:** Full system control, user management.
2. **Maintenance Staff:** Equipment updates, maintenance tasks, receives equipment alerts.
3. **Security Staff:** Access log monitoring, incident investigation, security alarms.
4. **General User:** View-only access to reports and information.

Each role extends `User` class and implements `AlertListener` appropriately.

### 4.8 Data Persistence (I/O)
`FileHandler` provides:
- Serialization for building data.
- Export reports to text files.
- Import configuration files.
- User credentials storage and validation.
- System event logging.
- Uses `Scanner` for reading user input from console.

## 5. How to Compile and Run

### Compilation:

```bash
# Navigate to project root
cd "E:\OOPS\OOPS Project"

# Create output directory
mkdir bin

# Compile all source files
javac -d bin src/com/smartbuilding/*.java src/com/smartbuilding/model/*.java src/com/smartbuilding/service/*.java src/com/smartbuilding/exception/*.java src/com/smartbuilding/util/*.java
```

### Running the Application:

```bash
# Run the main class
java -cp bin com.smartbuilding.SmartBuildingApp
```

### Sample Demo Credentials:
- **Administrator:** username: `admin`, password: `admin123`
- **Maintenance Staff:** username: `staff`, password: `staff123`
- **Security Staff:** username: `security`, password: `sec123`
- **General User:** username: `user`, password: `user123`

## 6. Sample Workflow

1. System starts and initializes demo data (2 floors, rooms, equipment, lights).
2. User logs in with appropriate role.
3. Depending on role, user can:
   - Add/remove rooms (admin only)
   - Add equipment to rooms
   - Update room occupancy
   - Trigger alerts manually
   - View various reports (energy, occupancy, equipment, security, lighting)
   - Control outdoor lighting
   - Perform security operations (log access, report incidents, trigger alarms)
   - Save/load data to/from files
4. Alerts are automatically notified to subscribed listeners.
5. All actions are logged.

## 7. Exception Handling Scenarios (3+ cases)

1. **InvalidAccessException** - when a user performs unauthorized action:
   ```java
   if (!(currentUser instanceof Administrator)) {
       throw new InvalidAccessException(currentUser.getUsername(), "Add Room");
   }
   ```

2. **EquipmentNotFoundException** - when equipment/room ID not found:
   ```java
   Room room = findRoomById(roomId);
   if (room == null) {
       throw new EquipmentNotFoundException(roomId);
   }
   ```

3. **InvalidInputException** - when invalid data provided (e.g., occupancy > capacity):
   ```java
   if (count < 0 || count > capacity) {
       throw new Exception("Invalid occupancy count...");
   }
   ```

4. **FileOperationException** - when file I/O fails:
   ```java
   try (ObjectOutputStream oos = ...) { ... } catch (IOException e) {
       throw new FileOperationException(filename, "SAVE");
   }
   ```

## 8. Conclusion

This project successfully demonstrates all required OOPS concepts and functional requirements. The system is modular, extensible, and follows best practices. All code is well-indented, commented, and compiles/runs error-free.

---

**Note:** The code files are provided separately in the `src/` directory. This document includes a high-level overview and design explanation. For detailed implementation, refer to the individual Java files.
