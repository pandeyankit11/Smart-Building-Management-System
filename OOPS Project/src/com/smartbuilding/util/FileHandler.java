package com.smartbuilding.util;

import com.smartbuilding.exception.FileOperationException;
import com.smartbuilding.model.*;
import com.smartbuilding.service.AlertSystem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * FileHandler class handles all file I/O operations.
 * Demonstrates file handling, Scanner usage, and exception handling.
 */
public class FileHandler {
    private String dataDirectory;

    public FileHandler(String dataDirectory) {
        if (dataDirectory == null || dataDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("Data directory is required");
        }
        this.dataDirectory = dataDirectory;
        try {
            Files.createDirectories(Path.of(dataDirectory));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create data directory: " + dataDirectory, e);
        }
    }

    public void saveBuildingData(Building building, AlertSystem alertSystem) throws FileOperationException {
        String filename = dataDirectory + "/building_data.ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(new SavedState(building, alertSystem));
            System.out.println("Building data saved to: " + filename);
        } catch (IOException e) {
            throw new FileOperationException("Failed to save building data: " + filename, e);
        }
    }

    public SavedState loadBuildingData() throws FileOperationException {
        String filename = dataDirectory + "/building_data.ser";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object loaded = ois.readObject();
            if (!(loaded instanceof SavedState)) {
                throw new FileOperationException("Saved data has an unsupported format: " + filename);
            }
            System.out.println("Building data loaded from: " + filename);
            return (SavedState) loaded;
        } catch (FileNotFoundException e) {
            throw new FileOperationException("No saved data found: " + filename, e);
        } catch (IOException | ClassNotFoundException e) {
            throw new FileOperationException("Failed to load building data: " + filename, e);
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
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            writer.println("User ID: " + user.getUserId());
            writer.println("Username: " + user.getUsername());
            writer.println("Role: " + user.getRole());
            writer.println("Password Salt: " + Base64.getEncoder().encodeToString(salt));
            writer.println("Password Hash: " + hashPassword(password, salt));
            writer.println("Created: " + java.time.LocalDateTime.now());
            System.out.println("User credentials saved: " + user.getUsername());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new FileOperationException("Failed to save user credentials: " + filename, e);
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
                String passwordSalt = null;
                String passwordHash = null;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Username:")) {
                        fileUsername = valueAfterColon(line);
                    } else if (line.startsWith("Password Salt:")) {
                        passwordSalt = valueAfterColon(line);
                    } else if (line.startsWith("Password Hash:")) {
                        passwordHash = valueAfterColon(line);
                    }
                }
                if (username.equals(fileUsername) && passwordSalt != null && passwordHash != null
                        && passwordHash.equals(hashPassword(password,
                        Base64.getDecoder().decode(passwordSalt)))) {
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

    private static String valueAfterColon(String line) {
        int separator = line.indexOf(':');
        return separator >= 0 ? line.substring(separator + 1).trim() : "";
    }

    private static String hashPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec specification = new PBEKeySpec(password.toCharArray(), salt, 65_536, 256);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(factory.generateSecret(specification).getEncoded());
        } finally {
            specification.clearPassword();
        }
    }

    public static final class SavedState implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Building building;
        private final AlertSystem alertSystem;

        private SavedState(Building building, AlertSystem alertSystem) {
            this.building = building;
            this.alertSystem = alertSystem;
        }

        public Building getBuilding() {
            return building;
        }

        public AlertSystem getAlertSystem() {
            return alertSystem;
        }
    }
}
