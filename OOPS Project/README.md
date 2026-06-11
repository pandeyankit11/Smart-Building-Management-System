# Smart Building Management System - OOPS Project

## Submission Overview

This folder contains the complete submission for the Smart Building Management System project, fulfilling all specified OOPS requirements.

## Folder Structure

```
E:\OOPS\OOPS Project\
├── src/
│   └── com/
│       └── smartbuilding/
│           ├── SmartBuildingApp.java       (Main class)
│           ├── model/                      (Entity classes)
│           ├── service/                    (Service classes)
│           ├── exception/                  (Custom exceptions)
│           └── util/                       (File handler)
├── bin/                                   (Compiled .class files - generated)
├── PROJECT_DOCUMENTATION.md               (Detailed documentation)
├── RUBRICS.md                             (Requirements mapping table)
├── FULL_PROJECT_REPORT.md                (Combined doc with code - optional)
└── README.md                              (This file)
```

## Deliverables Checklist

✅ **1. Word Document** - PROJECT_DOCUMENTATION.md
   - Comprehensive project explanation
   - UML diagram description (class relationships)
   - Feature details
   - OOPS principles mapping

✅ **2. Code Appended to Word Doc** - FULL_PROJECT_REPORT.md (or separate .java files)
   - Complete source code of all 24 Java files included

✅ **3. .Java Files** - All source files in `src/` directory
   - 24 Java files organized in packages

✅ **4. PPT** - Optional (not provided; can be created from documentation)

✅ **5. Rubrics Table** - RUBRICS.md
   - Detailed table showing usage of all 12 required components
   - Count and specific examples for each

## OOPS Requirements Met

| Requirement | Status | Count/Details |
|-------------|--------|---------------|
| Classes (≥4) | ✅ | 18+ |
| Nested Classes (≥1) | ✅ | 7 |
| Abstract Class (≥1) | ✅ | 1 (BuildingComponent) |
| Interface (≥1) | ✅ | 1 (AlertListener) |
| Hierarchical Inheritance (≥1) | ✅ | User → 4 subclasses |
| Package | ✅ | com.smartbuilding + subpackages |
| Exception Handling (≥3 cases) | ✅ | 4+ custom exceptions |
| I/O (File, Scanner) | ✅ | FileHandler with multiple streams |
| Overloaded Methods (≥3) | ✅ | 15+ |
| Overloaded Constructors (≥2) | ✅ | 15+ |
| Vararg Overloading (≥1) | ✅ | 5 methods |
| Wrapper Classes | ✅ | Used throughout |

## How to Compile

```bash
# Navigate to project directory
cd "E:\OOPS\OOPS Project"

# Compile all source files
javac -d bin src/com/smartbuilding/*.java src/com/smartbuilding/model/*.java src/com/smartbuilding/service/*.java src/com/smartbuilding/exception/*.java src/com/smartbuilding/util/*.java
```

## How to Run

```bash
java -cp bin com.smartbuilding.SmartBuildingApp
```

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Administrator | admin | admin123 |
| Maintenance Staff | staff | staff123 |
| Security Staff | security | sec123 |
| General User | user | user123 |

## System Features

1. **Building Management** - Add rooms/floors, manage equipment
2. **Lighting Control** - Automated outdoor lighting with schedules
3. **Security** - Access logs, incidents, alarms
4. **Occupancy** - Real-time tracking, peak analysis, utilities optimization
5. **Alerts** - Equipment failures, security breaches, maintenance reminders
6. **Reports** - Energy, occupancy, equipment, security, lighting

## Notes

- All code compiles error-free and runs successfully.
- Code is original, well-indented, and extensively commented.
- Exception handling implemented for all critical operations.
- Follows Java coding conventions and OOPS best practices.

---

**Project Completed:** April 2026
**Technology:** Java (JDK 17+ recommended)
**Total Source Files:** 24
