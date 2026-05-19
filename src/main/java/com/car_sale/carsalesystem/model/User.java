package com.car_sale.carsalesystem.model;

/**
 * Base User class – demonstrates Encapsulation.
 * All fields are private; access is via getter/setter methods.
 */
public class User {

    // ── Private fields (Encapsulation) ────────────────────────────────────────
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role; // "BUYER" | "SELLER"

    // ── Constructors ──────────────────────────────────────────────────────────
    public User() {}

    public User(String userId, String name, String email,
                String phone, String password, String role) {
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.password = password;
        this.role     = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getUserId()              { return userId; }
    public void   setUserId(String userId) { this.userId = userId; }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getPassword()                  { return password; }
    public void   setPassword(String password)   { this.password = password; }

    public String getRole()              { return role; }
    public void   setRole(String role)   { this.role = role; }

    // ── Display method (overridden via Polymorphism in subclasses) ─────────────
    public String displayInfo() {
        return String.format("User[id=%s, name=%s, email=%s, phone=%s, role=%s]",
                userId, name, email, phone, role);
    }

    /**
     * Serialises a user to a pipe-delimited line for users.txt storage.
     * Format: userId|name|email|phone|password|role
     */
    public String toFileString() {
        return String.join("|", userId, name, email, phone, password, role);
    }

    /**
     * Deserialises a pipe-delimited line from users.txt into the appropriate subtype.
     */
    public static User fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;

        String role = parts[5];
        if ("BUYER".equalsIgnoreCase(role)) {
            return new Buyer(parts[0], parts[1], parts[2], parts[3], parts[4]);
        } else if ("SELLER".equalsIgnoreCase(role)) {
            return new Seller(parts[0], parts[1], parts[2], parts[3], parts[4]);
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(parts[0], parts[1], parts[2], parts[3], parts[4]);
        } else {
            return new User(parts[0], parts[1], parts[2], parts[3], parts[4], role);
        }
    }
}
