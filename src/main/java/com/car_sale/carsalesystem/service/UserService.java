package com.car_sale.carsalesystem.service;

import com.car_sale.carsalesystem.model.User;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserService – all CRUD operations backed by a plain text file (users.txt).
 * File format per line: userId|name|email|phone|password|role
 */
@Service
public class UserService {

    // File is stored at the project root (next to pom.xml) for simplicity
    private static final String FILE_PATH = "users.txt";

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new user.
     *
     * @return true on success, false if email already taken.
     */
    public boolean registerUser(User user) {
        // Validate uniqueness by email
        if (findByEmail(user.getEmail()) != null) {
            return false;
        }
        // Auto-generate userId if blank
        if (user.getUserId() == null || user.getUserId().isBlank()) {
            user.setUserId(generateId());
        }
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) { // append mode
            bw.write(user.toFileString());
            bw.newLine();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Could not write to users.txt", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all users stored in users.txt. */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_PATH))) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    User u = User.fromFileString(line);
                    if (u != null) users.add(u);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read users.txt", e);
        }
        return users;
    }

    /** Finds a user by their unique ID. */
    public User findById(String userId) {
        return getAllUsers().stream()
                .filter(u -> u.getUserId().equalsIgnoreCase(userId))
                .findFirst()
                .orElse(null);
    }

    /** Finds a user by email address. */
    public User findByEmail(String email) {
        return getAllUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTHENTICATE (Login)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks credentials.
     *
     * @return the matching User, or null if credentials are wrong.
     */
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates name, phone, and/or password for an existing user.
     *
     * @return true if the user was found and updated.
     */
    public boolean updateUser(String userId, String newName,
                              String newPhone, String newPassword) {
        List<User> users = getAllUsers();
        boolean found = false;
        for (User u : users) {
            if (u.getUserId().equalsIgnoreCase(userId)) {
                if (newName     != null && !newName.isBlank())     u.setName(newName);
                if (newPhone    != null && !newPhone.isBlank())    u.setPhone(newPhone);
                if (newPassword != null && !newPassword.isBlank()) u.setPassword(newPassword);
                found = true;
                break;
            }
        }
        if (found) rewriteFile(users);
        return found;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Removes a user account permanently.
     *
     * @return true if the user existed and was removed.
     */
    public boolean deleteUser(String userId) {
        List<User> users = getAllUsers();
        List<User> filtered = users.stream()
                .filter(u -> !u.getUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
        if (filtered.size() == users.size()) return false; // not found
        rewriteFile(filtered);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Rewrites the entire users.txt with the given list. */
    private void rewriteFile(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (User u : users) {
                bw.write(u.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not rewrite users.txt", e);
        }
    }

    /** Generates a simple unique user ID based on current time. */
    private String generateId() {
        return "U" + System.currentTimeMillis();
    }
}
