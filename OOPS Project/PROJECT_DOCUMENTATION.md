# Smart Building Management System: Technical Documentation

## 1. Purpose

The Smart Building Management System (SBMS) is an educational Java application that models and coordinates the major operational areas of a building: infrastructure, equipment, energy, occupancy, lighting, security, alerts, users, persistence, and reporting.

The implementation uses only the Java standard library and runs on JDK 11+.

## 2. Architecture

The project follows a layered package structure:

```text
com.smartbuilding
├── SmartBuildingApp              Application entry point
├── model                         Domain objects and OOP hierarchies
├── service                       Use cases and coordination
├── exception                     Domain-specific checked exceptions
└── util                          IDs and file operations
```

### Domain Layer

`Building` aggregates `Floor`; each `Floor` aggregates `Room`; each `Room` aggregates `Equipment`. `Building` also owns `LightingSystem`, `SecuritySystem`, and `OccupancyMonitor`.

`BuildingComponent` is the abstract base for maintainable building elements:

```text
BuildingComponent
├── Building
├── Floor
├── Room
├── Equipment
├── LightingSystem
├── SecuritySystem
└── OccupancyMonitor
```

`User` is the base of a hierarchical inheritance model:

```text
User
├── Administrator
├── MaintenanceStaff
├── SecurityStaff
└── GeneralUser
```

All four concrete user types implement `AlertListener`, demonstrating interface-based polymorphism.

### Service Layer

- `BuildingManager`: application facade, authorization, console workflow, save/load coordination
- `AlertSystem`: alert creation, one-time listener delivery, filtering, and resolution
- `ReportGenerator`: energy, occupancy, equipment, security, lighting, and combined reports

### Utility Layer

- `IdGenerator`: thread-safe process-wide readable IDs using `AtomicLong`
- `FileHandler`: serialization, report export, configuration import, event logs, and hashed credentials

## 3. OOP Design

### Encapsulation

Fields are private or protected and accessed through behavior-focused methods. Mutable lists and maps are returned as defensive copies.

### Abstraction

`BuildingComponent` defines the common state and abstract `performMaintenance()` contract. Each subclass supplies its own maintenance behavior.

### Inheritance and Polymorphism

Building components override `performMaintenance()`. User subclasses implement `AlertListener` differently, so `AlertSystem` can notify them through a shared interface.

### Composition and Aggregation

The building object graph expresses real ownership relationships. Nested classes keep subordinate concepts close to their owners:

- `Equipment.EquipmentSpecs`
- `LightingSystem.Light`
- `SecuritySystem.AccessLog`
- `SecuritySystem.Incident`
- `SecuritySystem.Alarm`
- `OccupancyMonitor.OccupancyRecord`
- `Alert.AlertTypeEnum`

## 4. Functional Modules

### Infrastructure and Equipment

- Add floors, rooms, and equipment
- Find rooms and equipment by stable IDs
- Prevent duplicate floor numbers and component IDs
- Track equipment type, status, energy, specifications, and maintenance dates
- Validate status and non-negative energy values

### Occupancy

- Enforce room capacity
- Increment, decrement, and directly update occupancy
- Record time-stamped occupancy samples
- Calculate room and building utilization
- Detect near-capacity records and analyze peak periods

### Lighting

- Add uniquely identified lights
- Turn individual/all lights on or off
- Validate brightness from 0 to 100
- Support daytime and overnight schedules
- Track cumulative energy use

### Security

- Store authorized and unauthorized access logs
- Create incidents and move them through `OPEN`, `INVESTIGATING`, and `RESOLVED`
- Trigger and deactivate alarms
- Produce security summaries

### Alerts

- Create single or multiple alerts
- Validate `INFO`, `WARNING`, `HIGH`, and `CRITICAL` severity
- Deliver each alert once to matching listeners
- Filter by severity and move resolved alerts to history

### Reports

Reports are generated from live domain state:

- Energy consumption
- Current occupancy and capacity
- Correct per-type equipment totals and statuses
- Security events and active alarms
- Lighting details and consumption
- Combined reports via varargs

## 5. Role-Based Access

| Operation | Admin | Maintenance | Security | General |
|---|---:|---:|---:|---:|
| View reports/status | Yes | Yes | Yes | Yes |
| Add room | Yes | No | No | No |
| Add equipment | Yes | Yes | No | No |
| Update occupancy | Yes | Yes | Yes | No |
| Control lighting | Yes | Yes | No | No |
| Trigger alerts | Yes | Yes | Yes | No |
| Security operations | Yes | No | Yes | No |

Unauthorized operations throw `InvalidAccessException`.

## 6. Persistence and I/O

`FileHandler.saveBuildingData()` serializes a `SavedState` containing the full building and alert object graphs. All participating domain types are serializable and define stable `serialVersionUID` values.

`BuildingManager.loadData()` replaces the current state, rebuilds report dependencies, and clears the current login.
Alert listeners are transient, so user sessions and in-memory passwords are not written into the state file.

Other I/O functions:

- Text report export
- Configuration-file import
- Timestamped event logging
- User credential storage using random salts and PBKDF2-HMAC-SHA256 hashes

## 7. Validation and Exceptions

- `InvalidAccessException`: role or login failure
- `EquipmentNotFoundException`: unknown room/equipment target
- `InvalidInputException`: invalid occupancy, brightness, menu number, or status
- `FileOperationException`: save, load, import, export, or credential I/O failure

The interactive loop reads complete lines and parses them, allowing malformed input to be handled without leaving `Scanner` in a broken state.

## 8. Build and Verification

```bash
./build.sh
./test.sh   # Verified: 9 passed, 0 failed + console smoke test
./run.sh
```

The test harness covers:

1. Demo topology, counts, and ID uniqueness
2. Equipment totals, statuses, and energy reports
3. Occupancy boundaries
4. Lighting behavior and invalid values
5. Security incident lifecycle
6. Alert delivery and resolution
7. Role-based access
8. Full save/restore
9. Configuration, credentials, and report files
10. Invalid console input smoke path

Verified on OpenJDK 11.0.31 with a warning-clean `javac -Xlint:all` build.

## 9. Extension Points

The service boundaries allow later replacement of console I/O, serialized storage, and hardcoded demo authentication with a GUI/web layer, database repositories, or an identity provider without redesigning the core domain hierarchy.
