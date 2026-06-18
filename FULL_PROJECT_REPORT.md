# Smart Building Management System: Full Project Report

## Executive Summary

This project implements a complete Smart Building Management System in Java.
It demonstrates core object-oriented programming requirements while providing
working building infrastructure, equipment, energy, occupancy, lighting,
security, alerts, user authorization, reporting, and persistence workflows.

The original project was console-based. The current version adds an interactive
frontend and Spring Boot REST API while preserving the original console
application and OOP engine.

## Objective

The goal is to provide a user-friendly system that still clearly demonstrates
OOP concepts such as classes, objects, encapsulation, inheritance, abstraction,
polymorphism, interfaces, nested classes, overloading, constructors, custom
exceptions, file I/O, and collections.

## Implementation Status

The project currently includes:

- 25+ production Java source files in the original OOP engine.
- Complete building hierarchy: building, floors, rooms, equipment, lighting,
  security, occupancy, and alerts.
- Four demo user roles with role-based access control.
- Console application with 12 menu options.
- Browser frontend with the same 12 menu options.
- Spring Boot REST API that delegates to the original `BuildingManager`.
- Full object-state save/restore capability.
- Energy, occupancy, equipment, security, lighting, and combined reports.
- Automated tests for both console/core behavior and API/frontend behavior.

## Frontend Update

The interactive frontend is located at:

```text
api/src/main/resources/static/index.html
```

It supports:

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

The frontend uses fetch requests to communicate with REST endpoints under
`/api`. Validation and role errors are returned as JSON and displayed in the UI.

## API Update

`SmartBuildingController` exposes the original service operations through REST.
The controller handles:

- Session state
- Building/floor/room/equipment CRUD
- Occupancy changes
- Lighting controls
- Security logs/incidents/alarms
- Alerts
- Reports
- Save/load
- Complete status

The API keeps business rules in the Java OOP layer instead of duplicating them
in the frontend.

## Verification Results

All major components have been tested.

| Area | Result |
| --- | --- |
| Maven API/frontend suite | 8 tests, 0 failures |
| Original console/OOP suite | 9 passed, 0 failed |
| Console smoke test | Passed |
| Java compilation | Successful |

The API/frontend tests cover all 12 menu groups, including role checks,
validation errors, CRUD actions, reports, status, save/load, and frontend menu
rendering.

The original console/OOP tests cover:

- Demo topology and unique IDs
- Reports and energy totals
- Occupancy validation
- Lighting controls and invalid brightness
- Security incident lifecycle
- Alert delivery and resolution
- Role-based access
- Persistence
- File handling and credential hashing
- Console smoke behavior

## Build and Verification Commands

Run frontend/API tests:

```bash
cd /home/kratos/Documents/SBMS
mvn test
```

Run original console/OOP tests:

```bash
cd "/home/kratos/Documents/SBMS/OOPS Project"
./test.sh
```

Start frontend:

```bash
cd /home/kratos/Documents/SBMS
mvn spring-boot:run
```

Open:

```text
http://localhost:8081/
```

Start console:

```bash
cd "/home/kratos/Documents/SBMS/OOPS Project"
./run.sh
```

## Limitations

- Demo login credentials are hardcoded.
- Persistence uses Java serialization instead of a database.
- The frontend is a static HTML/JavaScript app.
- Browser "Exit" logs out/ends the UI session; it does not shut down the server.

## Conclusion

SBMS now satisfies the original OOP project goals and adds a practical
interactive frontend. The implementation remains centered around the Java OOP
model while offering a more user-friendly interface for demonstrating and using
the 12 original system features.
