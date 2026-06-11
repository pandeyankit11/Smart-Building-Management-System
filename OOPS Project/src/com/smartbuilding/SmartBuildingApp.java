package com.smartbuilding;

import com.smartbuilding.model.*;
import com.smartbuilding.service.BuildingManager;
import com.smartbuilding.exception.*;

/**
 * SmartBuildingApp - Main application entry point.
 * Smart Building Management System for residential areas with disaster alert capabilities.
 *
 * Features:
 * - Building Infrastructure Management
 * - Lighting Management (outdoor)
 * - Security System Management
 * - Occupancy Monitoring
 * - Alerts and Notifications
 * - Reporting and Analytics
 * - Role-based access control
 *
 * Demonstrates all OOPS principles and requirements.
 */
public class SmartBuildingApp {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  SMART BUILDING MANAGEMENT SYSTEM");
        System.out.println("  Disaster Alert & Infrastructure Management");
        System.out.println("==================================================\n");

        try {
            BuildingManager manager = new BuildingManager("Smart Residential Complex", "123 Innovation Drive");

            // Initialize with demo data
            manager.initializeDemoData();

            // Display initial status
            System.out.println("\n=== INITIAL SYSTEM STATUS ===");
            System.out.println(manager.getBuilding());

            // Run interactive menu
            manager.runInteractiveMenu();

        } catch (Exception e) {
            // Comprehensive exception handling with type checking
            if (e instanceof InvalidAccessException) {
                System.err.println("ACCESS ERROR: " + e.getMessage());
                InvalidAccessException iae = (InvalidAccessException) e;
                if (iae.getUserId() != null) {
                    System.err.println("  User: " + iae.getUserId());
                    System.err.println("  Operation: " + iae.getAttemptedOperation());
                }
            } else if (e instanceof EquipmentNotFoundException) {
                System.err.println("EQUIPMENT ERROR: " + e.getMessage());
            } else if (e instanceof FileOperationException) {
                System.err.println("FILE I/O ERROR: " + e.getMessage());
            } else {
                System.err.println("SYSTEM ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n=== SYSTEM SHUTDOWN ===");
        System.out.println("Thank you for using Smart Building Management System!");
    }
}
