package com.smartbuilding.util;

import com.smartbuilding.exception.FileOperationException;
import com.smartbuilding.model.*;
import com.smartbuilding.service.AlertSystem;
import com.smartbuilding.service.ReportGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * FileHandler class handles all file I/O operations.
 * Demonstrates file handling, Scanner usage, and exception handling.
 */
public class FileHandler {
    private String dataDirectory;

    public FileHandler(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void saveBuildingData(Building building, AlertSystem alertSystem) throws FileOperationException {
        String filename = dataDirectory + "/building_data.ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            // Note: In a real implementation, all model classes would implement Serializable
            // For demonstration, we're saving simple string representations
            List<String> data = new ArrayList<>();
            data.add(building.toString());
            data.add("Floors: " + building.getFloors().size());
            data.add("Alerts: " + alertSystem.getActiveAlertCount());
            oos.writeObject(data);
            System.out.println("Building data saved to: " + filename);
        } catch (IOException e) {
            throw new FileOperationException(filename, "SAVE");
        }
    }

    public void loadBuildingData() throws FileOperationException {
        String filename = dataDirectory + "/building_data.ser";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<String> data = (List<String>) ois.readObject();
            System.out.println("Building data loaded from: " + filename);
            for (String line : data) {
                System.out.println("  " + line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            throw new FileOperationException(filename, "LOAD");
        }
    }

    public void exportReportToFile(String report, String reportType) throws FileOperationException {
        String filename = dataDirectory + "/" + reportType + "_report_" +
                         System.currentTimeMillis() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
            System.out.println("Report exported to: " + filename);
        } catch (IOException e) {
            throw new FileOperationException(filename, "EXPORT");
        }
    }

    public String importConfiguration(String configFile) throws FileOperationException {
        StringBuilder config = new StringBuilder();
        String filename = dataDirectory + "/" + configFile;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            System.out.println("Importing configuration from: " + filename);
            while ((line = reader.readLine()) != null) {
                config.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new FileOperationException(filename, "IMPORT");
        }
        return config.toString();
    }

    public void logEvent(String eventType, String message) {
        String logFile = dataDirectory + "/system_log.txt";
        try (FileWriter writer = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(writer);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("[" + java.time.LocalDateTime.now() + "] [" + eventType + "] " + message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public void saveUserCredentials(User user, String password) throws FileOperationException {
        String filename = dataDirectory + "/users/" + user.getUserId() + ".txt";
        File userDir = new File(dataDirectory + "/users");
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("User ID: " + user.getUserId());
            writer.println("Username: " + user.getUsername());
            writer.println("Role: " + user.getRole());
            writer.println("Password: " + password);
            writer.println("Created: " + java.time.LocalDateTime.now());
            System.out.println("User credentials saved: " + user.getUsername());
        } catch (IOException e) {
            throw new FileOperationException(filename, "SAVE_USER");
        }
    }

    public boolean validateUserCredentials(String username, String password) {
        File userDir = new File(dataDirectory + "/users");
        if (!userDir.exists()) return false;

        File[] files = userDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return false;

        for (File file : files) {
            try (Scanner scanner = new Scanner(file)) {
                String fileUsername = null;
                String filePassword = null;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Username:")) {
                        fileUsername = line.split(":")[1].trim();
                    } else if (line.startsWith("Password:")) {
                        filePassword = line.split(":")[1].trim();
                    }
                }
                if (username.equals(fileUsername) && password.equals(filePassword)) {
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Error reading user file: " + file.getName());
            }
        }
        return false;
    }

    // Getters
    public String getDataDirectory() { return dataDirectory; }
}
