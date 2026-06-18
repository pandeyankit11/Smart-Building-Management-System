# Smart Building Management System

A Java 11 Smart Building Management System for managing building
infrastructure, equipment, occupancy, lighting, security, alerts, reports,
users, and persistent system state.

The original project was a console-based OOP application with a 12-option menu.
The current project keeps that console application intact and adds an
interactive browser frontend backed by a Spring Boot REST API.

## Highlights

- **Interactive frontend** - browser UI for all 12 original menu options.
- **Original console preserved** - `OOPS Project/run.sh` still runs the terminal
  workflow.
- **Robust validation** - duplicate floors are rejected, occupancy cannot exceed
  room capacity, brightness is limited to 0-100, and malformed inputs return
  clear errors.
- **Full OOP hierarchy** - 25+ production Java classes, abstract
  `BuildingComponent`, nested types, user inheritance, varargs, overloading,
  and custom exceptions.
- **Role-based access control** - administrator, maintenance, security, and
  general user roles.
- **Persistence** - save/load through Java serialization.
- **Reporting engine** - energy, occupancy, equipment, security, lighting, and
  combined reports.
- **Alert system** - listener pattern, severity validation, active/resolved
  alert tracking.
- **Automated verification** - API/frontend tests plus original console/OOP
  tests.

## The 12 Menu Options

The frontend and console both support:

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

## Current Verification Status

- Maven API/frontend suite: **8 tests, 0 failures**.
- Original console/OOP suite: **9 passed, 0 failed**.
- Console smoke test: **passed**.
- Java compilation: warning-clean for the original OOP test build.
- Last verified: **2026-06-18**.

## Requirements

- JDK 11 or newer
- Maven
- Bash for the original console scripts

## Run the Interactive Frontend

The Spring Boot app defaults to port `8081` to avoid common `8080` conflicts.

```bash
cd /home/kratos/Documents/SBMS
mvn spring-boot:run
```

Open:

```text
http://localhost:8081/
```

Use a different port if needed:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

## Run the Original Console App

```bash
cd "/home/kratos/Documents/SBMS/OOPS Project"
./run.sh
```

## Demo Accounts

| Role | Username | Password | Permissions |
| --- | --- | --- | --- |
| Administrator | `admin` | `admin123` | Full management |
| Maintenance | `staff` | `staff123` | Equipment, occupancy, lighting, alerts |
| Security | `security` | `sec123` | Occupancy, security operations, alerts |
| General user | `user` | `user123` | Limited/general access |

These credentials are hardcoded demo credentials in `BuildingManager`.

## Run Tests

Frontend/API tests:

```bash
cd /home/kratos/Documents/SBMS
mvn test
```

Original console/OOP tests:

```bash
cd "/home/kratos/Documents/SBMS/OOPS Project"
./test.sh
```

## Project Structure

```text
.
├── OOPS Project/
│   ├── src/com/smartbuilding/          Original OOP engine and console app
│   ├── test/com/smartbuilding/         Original OOP test harness
│   ├── build.sh
│   ├── run.sh
│   └── test.sh
├── api/
│   ├── src/main/java/                  Spring Boot API wrapper
│   ├── src/main/resources/static/      Interactive frontend
│   └── src/test/java/                  API/frontend tests
├── pom.xml                             Maven build for API + original sources
├── PROJECT_DOCUMENTATION.md
├── FULL_PROJECT_REPORT.md
└── RUBRICS.md
```

Generated directories:

- `target/` - Maven build output
- `OOPS Project/build/` - console build output
- `data/` - runtime saves, exported reports, and logs

## Documentation

- [`PROJECT_DOCUMENTATION.md`](PROJECT_DOCUMENTATION.md): architecture,
  features, API layer, frontend layer, and extension points.
- [`FULL_PROJECT_REPORT.md`](FULL_PROJECT_REPORT.md): objectives,
  implementation status, verification results, and project summary.
- [`RUBRICS.md`](RUBRICS.md): OOP requirement mapping with current frontend/API
  additions.
