# OOP Requirements Rubric

## Requirement Mapping

| # | Requirement | Minimum | Verified Implementation |
| --- | --- | ---: | --- |
| 1 | Classes | 4 | 25+ production Java files; key classes include `Building`, `Floor`, `Room`, `Equipment`, `BuildingManager`, `AlertSystem`, `ReportGenerator`, and the Spring API classes |
| 2 | Nested class | 1 | `EquipmentSpecs`, `Light`, `AccessLog`, `Incident`, `Alarm`, `OccupancyRecord`, `AlertTypeEnum`, and API DTO helper types |
| 3 | Abstract class | 1 | `BuildingComponent` with abstract `performMaintenance()` |
| 4 | Interface | 1 | `AlertListener`, implemented by alert-capable user classes |
| 5 | Hierarchical inheritance | 1 | `User` to `Administrator`, `MaintenanceStaff`, `SecurityStaff`, `GeneralUser`; `BuildingComponent` to component classes |
| 6 | Packages | 1 | `com.smartbuilding` with `model`, `service`, `exception`, `util`, plus `com.smartbuilding.api` |
| 7 | Exception handling | 3 cases | Custom exceptions plus REST exception handlers that return JSON errors |
| 8 | I/O | 1 | Object streams, buffered text I/O, `PrintWriter`, `Scanner`, `Files`, serialization, logs, config import, report export |
| 9 | Overloaded methods | 3 | Examples in `Equipment`, `AlertSystem`, `SecuritySystem`, `ReportGenerator`, `BuildingManager`, and user subclasses |
| 10 | Overloaded constructors | 2 | Examples in `Building`, `Room`, `Equipment`, `User`, and user subclasses |
| 11 | Varargs | 1 | `createAlerts`, `sendMaintenanceReminder`, `addMultipleLights`, `detectUnauthorizedAccesses`, and combined reports |
| 12 | Wrapper classes | Required | `Integer`, `Double`, and `Boolean` through typed collections, maps, stream counts, DTOs, and autoboxing |
| 13 | User interface | Required | Original terminal menu plus interactive web frontend |
| 14 | Testing | Required | 8 API/frontend tests and 9 original console/OOP tests pass |

## OOP Principles

| Principle | Evidence |
| --- | --- |
| Encapsulation | Private/protected state, behavior methods, getters, validation, and defensive collection copies |
| Abstraction | Abstract `BuildingComponent`; interface-based `AlertListener`; service facade through `BuildingManager` |
| Inheritance | Component hierarchy and user hierarchy |
| Polymorphism | Overridden maintenance behavior and alert behavior called through base/interface types |
| Composition | Building/floor/room/equipment graph and system-owned lighting/security/occupancy modules |
| Association | `AlertSystem` associates with multiple `AlertListener` implementations |

## Overloading Evidence

### Methods

- `Equipment.updateStatus(String)` and `updateStatus(String, String)`
- `AlertSystem.createAlert(...)`
- `AlertSystem.resolveAlert(String)` and `resolveAlert(Alert)`
- `SecuritySystem.logAccess(...)`
- `SecuritySystem.generateSecurityReport()` and `generateSecurityReport(String)`
- `ReportGenerator.generateEnergyReport()` and `generateEnergyReport(String)`
- `ReportGenerator.generateOccupancyReport(...)`
- `ReportGenerator.generateEquipmentReport()` and `generateEquipmentReport(String)`
- `BuildingManager.login(...)`, `addRoom(...)`, and report methods

### Constructors

- `Room`: explicit ID, location convenience, capacity convenience, and floor
  convenience
- `Equipment`: explicit ID/specifications and convenience forms
- `User`: three forms
- `Building`, `Floor`, `LightingSystem`, `SecuritySystem`,
  `OccupancyMonitor`, and `Alert`: multiple forms
- User subclasses: multiple forms

## Exception Cases

| Exception | Trigger Example |
| --- | --- |
| `InvalidAccessException` | General user attempts an admin-only operation |
| `EquipmentNotFoundException` | Operation targets an unknown room/equipment ID |
| `InvalidInputException` | Occupancy exceeds capacity, brightness exceeds 100, or menu number is malformed |
| `FileOperationException` | Save/load/import/export operation fails |

REST endpoints convert these exceptions into JSON error responses for the
frontend.

## I/O Evidence

- `ObjectOutputStream` / `ObjectInputStream`: full application state
- `BufferedReader` / `BufferedWriter`: configurations and reports
- `PrintWriter`: credentials and event logs
- `java.nio.file.Files`: directories and test fixtures
- `Scanner`: console input and credential-file input
- PBKDF2-HMAC-SHA256: protected password verification data in file examples

## Current 12-Option Feature Coverage

| Menu Option | Frontend/API Status | Test Evidence |
| --- | --- | --- |
| Login | Complete | valid/invalid demo login |
| Logout | Complete | logout endpoint and UI option |
| Building & Equipment | Complete | building update, floors, rooms, equipment CRUD |
| Occupancy | Complete | view, set, increment, decrement, capacity errors |
| Lighting | Complete | add/edit/delete, one/all on/off, schedule, auto |
| Security | Complete | access logs, incidents, alarms, surveillance, role checks |
| Alerts | Complete | add/edit/acknowledge/resolve/delete, severity validation |
| Reports | Complete | individual, combined, export |
| View Complete Status | Complete | `/api/status` |
| Save Data | Complete | `/api/save`, admin-only |
| Load Data | Complete | `/api/load`, admin-only |
| Exit | Complete | frontend session exit/logout behavior |

## Verification Matrix

| Area | Automated Evidence |
| --- | --- |
| API/frontend menu coverage | 8 Maven tests cover all 12 menu options |
| Domain graph | Demo counts and unique ID assertions |
| Reports | Equipment status and energy assertions |
| Validation | Occupancy, capacity, brightness, severity, duplicate floor failures |
| Security | Access, incidents, alarms, investigate/resolve lifecycle |
| Alerts | Listener delivery once, active/resolved counts, frontend lifecycle |
| Authorization | General denial and role-specific success cases |
| Persistence | Save, mutate, load, restored state |
| File handling | Config import, hashed credentials, report export |
| Console | Invalid input followed by successful exit |

Run API/frontend verification:

```bash
mvn test
```

Run original console verification:

```bash
cd "OOPS Project"
./test.sh
```

Verified result on 2026-06-18:

- API/frontend: **8 tests, 0 failures**
- Console/OOP: **9 passed, 0 failed**
- Console smoke test: **passed**
