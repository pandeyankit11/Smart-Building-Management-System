# OOP Requirements Rubric

## Requirement Mapping

| # | Requirement | Minimum | Verified Implementation |
|---|---|---:|---|
| 1 | Classes | 4 | 25 production `.java` files; key classes include `Building`, `Floor`, `Room`, `Equipment`, `BuildingManager`, `AlertSystem`, and `ReportGenerator` |
| 2 | Nested class | 1 | 7 nested types: `EquipmentSpecs`, `Light`, `AccessLog`, `Incident`, `Alarm`, `OccupancyRecord`, `AlertTypeEnum` |
| 3 | Abstract class | 1 | `BuildingComponent` with abstract `performMaintenance()` |
| 4 | Interface | 1 | `AlertListener`, implemented by all four user subclasses |
| 5 | Hierarchical inheritance | 1 | `User` to `Administrator`, `MaintenanceStaff`, `SecurityStaff`, `GeneralUser`; also `BuildingComponent` to 7 component classes |
| 6 | Packages | 1 | `com.smartbuilding` with `model`, `service`, `exception`, and `util` subpackages |
| 7 | Exception handling | 3 cases | 4 custom checked exceptions plus validation and I/O catch paths |
| 8 | I/O | 1 | Object streams, buffered text I/O, `PrintWriter`, `Scanner`, `Files`, serialization, logs, config import, report export |
| 9 | Overloaded methods | 3 | Examples in `Equipment`, `AlertSystem`, `SecuritySystem`, `ReportGenerator`, `BuildingManager`, and user subclasses |
| 10 | Overloaded constructors | 2 | Examples in `Building`, `Room`, `Equipment`, `User`, and all user subclasses |
| 11 | Varargs | 1 | `createAlerts`, `sendMaintenanceReminder`, `addMultipleLights`, `detectUnauthorizedAccesses`, and combined/comparison reports |
| 12 | Wrapper classes | Required | `Integer`, `Double`, and `Boolean` through typed collections, maps, stream counts, and autoboxing |

## OOP Principles

| Principle | Evidence |
|---|---|
| Encapsulation | Private state, behavior methods, getters, and defensive collection copies |
| Abstraction | Abstract `BuildingComponent`; interface-based `AlertListener` contract |
| Inheritance | Two broad hierarchies for components and users |
| Polymorphism | Overridden maintenance and alert behavior called through base/interface types |
| Composition | Building/floor/room/equipment graph and system-owned nested objects |
| Association | `AlertSystem` associates with multiple `AlertListener` implementations |

## Overloading Evidence

### Methods

- `Equipment.updateStatus(String)` and `updateStatus(String, String)`
- `AlertSystem.createAlert(...)`
- `AlertSystem.resolveAlert(String)` and `resolveAlert(Alert)`
- `SecuritySystem.logAccess(...)`
- `SecuritySystem.generateSecurityReport()` and `generateSecurityReport(String)`
- `ReportGenerator.generateEnergyReport()` and `generateEnergyReport(String)`
- Three `generateOccupancyReport(...)` forms
- `ReportGenerator.generateEquipmentReport()` and `generateEquipmentReport(String)`
- `BuildingManager.login(...)`, `addRoom(...)`, and report methods

### Constructors

- `Room`: explicit ID, location convenience, capacity convenience, and floor convenience
- `Equipment`: explicit ID/specifications and multiple convenience forms
- `User`: three forms
- `Building`, `Floor`, `LightingSystem`, `SecuritySystem`, `OccupancyMonitor`, and `Alert`: multiple forms
- All four user subclasses: multiple forms

## Exception Cases

| Exception | Trigger Example |
|---|---|
| `InvalidAccessException` | General user attempts to add a room |
| `EquipmentNotFoundException` | Equipment operation targets an unknown room ID |
| `InvalidInputException` | Occupancy exceeds capacity, brightness exceeds 100, or menu number is malformed |
| `FileOperationException` | Save file is missing/corrupt or an import/export operation fails |

## I/O Evidence

- `ObjectOutputStream` / `ObjectInputStream`: full application state
- `BufferedReader` / `BufferedWriter`: configurations and reports
- `PrintWriter`: credentials and event logs
- `java.nio.file.Files`: directories and test fixtures
- `Scanner`: console and credential-file input
- PBKDF2-HMAC-SHA256: protected password verification data

## Verification Matrix

| Area | Automated Evidence |
|---|---|
| Build quality | Warning-clean `javac -Xlint:all` |
| Domain graph | Demo counts and unique ID assertions |
| Reports | Equipment status and energy assertions |
| Validation | Occupancy, capacity, brightness, and energy failures |
| Security | Access, incidents, alarms, investigate/resolve lifecycle |
| Alerts | Listener delivery once, active/resolved counts |
| Authorization | General denial and administrator success |
| Persistence | Save, mutate, load, and restored state |
| File handling | Config import, hashed credentials, report export |
| Console | Invalid input followed by successful exit |

Run all verification with:

```bash
./test.sh
```

Verified result: **9 passed, 0 failed**, plus console smoke test.
