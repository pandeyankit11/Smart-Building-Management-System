package com.smartbuilding.model;

import com.smartbuilding.util.IdGenerator;
import com.smartbuilding.exception.InvalidAccessException;
import java.io.Serializable;

/**
 * User class - base class for all system users.
 * Demonstrates hierarchical inheritance - multiple user types extend this.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String userId;
    protected String username;
    protected String password;
    protected String role;
    protected boolean isLoggedIn;

    // Overloaded constructors (2+ required)
    public User(String userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isLoggedIn = false;
    }

    public User(String userId, String username, String role) {
        this(userId, username, "default123", role);
    }

    public User(String username, String role) {
        this.generateUserId(username);
        this.username = username;
        this.password = "default123";
        this.role = role;
        this.isLoggedIn = false;
    }

    // Overloaded method - can authenticate with different parameter combinations
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void login(String password) throws InvalidAccessException {
        if (authenticate(password)) {
            this.isLoggedIn = true;
            System.out.println("User " + username + " logged in successfully.");
        } else {
            throw new InvalidAccessException("Invalid credentials");
        }
    }

    public void logout() {
        this.isLoggedIn = false;
        System.out.println("User " + username + " logged out.");
    }

    public void updatePassword(String oldPassword, String newPassword) throws InvalidAccessException {
        if (!authenticate(oldPassword)) {
            throw new InvalidAccessException("Old password is incorrect");
        }
        this.password = newPassword;
        System.out.println("Password updated successfully.");
    }

    protected void generateUserId(String username) {
        this.userId = IdGenerator.next("USR");
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isLoggedIn() { return isLoggedIn; }

    @Override
    public String toString() {
        return "User ID: " + userId + ", Username: " + username + ", Role: " + role + ", Logged In: " + isLoggedIn;
    }
}
