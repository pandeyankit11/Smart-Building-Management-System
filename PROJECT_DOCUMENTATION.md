# Smart Building Management System: Technical Documentation

## 1. Purpose

The Smart Building Management System (SBMS) is an educational Java application
that models the major operational areas of a smart building: infrastructure,
equipment, energy, occupancy, lighting, security, alerts, users, persistence,
and reporting.

The original system was console-based. The current system keeps the console
application and adds a Spring Boot REST API plus an interactive browser
frontend.

## 2. Architecture

The project now has four main parts:

```text
OOPS Project/src                 Original Java OOP domain and console workflow
api/src/main/java                Spring Boot REST API
api/src/main/resources/static    Browser frontend
api/src/test/java                API/frontend integration tests
```

The original package structure remains:

```text
com.smartbuilding
├── SmartBuildingApp              Console application entry point
├── model                         Domain objects and OOP hierarchies
├── service                       Use cases and coordination
├── exception                     Domain-specific checked exceptions
└── util                          IDs and file operations
```

The Spring Boot wrapper is under:

```text
com.smartbuilding.api
├── SmartBuildingApiApplication
├── SmartBuildingController
└── CorsConfig
```

## 3. Domain Layer

`Building` aggregates `Floor`; each `Floor` aggregates `Room`; each `Room`
aggregates `Equipment`. `Building` also owns `LightingSystem`,
`SecuritySystem`, and `OccupancyMonitor`.

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

Alert-capable users implement `AlertListener`, demonstrating interface-based
polymorphism.

## 4. Service Layer

- `BuildingManager`: application facade, authorization, console workflow,
  building operations, save/load coordination.
- `AlertSystem`: alert creation, listener delivery, filtering, and resolution.
- `ReportGenerator`: energy, occupancy, equipment, security, lighting, and
  combined reports.

The REST API delegates to these services instead of duplicating business logic.

## 5. Frontend Layer

The frontend is a single-page HTML/CSS/JavaScript app at:

```text
api/src/main/resources/static/index.html
```

It exposes the same 12 main options as the terminal menu:

1. Login
2. Logout
3. Building & Equipment
4. Occupancy
5. Lighting
6. Security
7. Alerts
8. Reports
9. View Complete Status
10. Save Data
11. Load Data
12. Exit

The frontend uses `fetch()` to call `/api/...` endpoints and shows backend
validation/authorization errors to the user.

## 6. API Layer

Important endpoint groups:

| Area | Endpoints |
| --- | --- |
| Session | `/api/session`, `/api/login`, `/api/logout` |
| Building | `/api/building`, `/api/floors`, `/api/rooms`, `/api/equipment` |
| Occupancy | `/api/occupancy/{roomId}` and increment/decrement actions |
| Lighting | `/api/lighting`, `/api/lights`, schedule, auto, all-light controls |
| Security | access logs, incidents, alarms, surveillance |
| Alerts | active/resolved lists, add, edit, acknowledge, resolve, delete |
| Reports | `/api/reports/{type}`, `/api/reports/combined`, export |
| State | `/api/status`, `/api/save`, `/api/load` |

Errors are returned as JSON:

```json
{
  "code": "BAD_REQUEST",
  "message": "Invalid input..."
}
```

Access failures use:

```json
{
  "code": "ACCESS_ERROR",
  "message": "Login required..."
}
```

## 7. OOP Design

### Encapsulation

Fields are private or protected and accessed through behavior-focused methods.
Mutable collections are returned as defensive copies.

### Abstraction

`BuildingComponent` defines common component state and the abstract
`performMaintenance()` contract.

### Inheritance and Polymorphism

Building components override `performMaintenance()`. User subclasses implement
alert handling differently, while `AlertSystem` interacts with them through the
shared `AlertListener` interface.

### Composition and Aggregation

The building object graph expresses real ownership relationships. Nested
classes keep subordinate concepts close to their owners:

- `Equipment.EquipmentSpecs`
- `LightingSystem.Light`
- `SecuritySystem.AccessLog`
- `SecuritySystem.Incident`
- `SecuritySystem.Alarm`
- `OccupancyMonitor.OccupancyRecord`
- `Alert.AlertTypeEnum`

## 8. Functional Modules

### Infrastructure and Equipment

- Add, edit, delete, and view floors, rooms, and equipment.
- Prevent duplicate floor numbers and invalid IDs.
- Track equipment type, status, energy, specifications, and maintenance dates.

### Occupancy

- View room occupancy.
- Set occupancy directly.
- Add/remove one occupant.
- Prevent occupancy below 0 or above room capacity.

### Lighting

- Add, edit, delete, and view lights.
- Turn one light on/off.
- Turn all lights on/off.
- Set automatic schedule.
- Run automatic control.

### Security

- View/add/delete access logs.
- View/add/update/delete incidents.
- View/trigger/deactivate/delete alarms.
- Toggle surveillance state.

### Alerts

- View active and resolved alerts.
- Add, edit, acknowledge, resolve, and delete alerts.
- Validate alert severity.

### Reports

Reports are generated from live domain state:

- Energy
- Occupancy
- Equipment
- Security
- Lighting
- Combined

Reports can also be exported to the runtime data directory.

### Persistence

`FileHandler.saveBuildingData()` serializes a `SavedState` containing the
building and alert object graphs. `BuildingManager.loadData()` restores state,
rebuilds report dependencies, and clears the current login.

## 9. Role-Based Access

| Operation | Admin | Maintenance | Security | General |
| --- | ---: | ---: | ---: | ---: |
| View reports/status | Yes | Yes | Yes | Yes |
| Add/edit/delete floors | Yes | No | No | No |
| Add/edit/delete rooms | Yes | No | No | No |
| Add/edit/delete equipment | Yes | Yes | No | No |
| Update occupancy | Yes | Yes | Yes | No |
| Control lighting | Yes | Yes | No | No |
| Security operations | Yes | No | Yes | No |
| Alert operations | Yes | Yes | Yes | Limited |
| Save/load data | Yes | No | No | No |

Unauthorized operations throw `InvalidAccessException`.

## 10. Build and Verification

Run the web/API test suite:

```bash
cd /home/kratos/Documents/SBMS
mvn test
```

Run the original console/OOP suite:

```bash
cd "/home/kratos/Documents/SBMS/OOPS Project"
./test.sh
```

Latest verification:

- API/frontend suite: 8 tests, 0 failures.
- Original console/OOP suite: 9 passed, 0 failed.
- Console smoke test: passed.

## 11. Extension Points

- Replace hardcoded demo login with real user registration.
- Replace serialized files with a database.
- Add charts for reports.
- Add browser end-to-end tests.
- Split frontend into a framework-based app if the UI grows.
