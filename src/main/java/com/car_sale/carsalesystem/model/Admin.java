package com.car_sale.carsalesystem.model;

/**
 * Admin – specialized User subclass.
 * Inherits from User (Inheritance).
 * Overrides displayInfo() for polymorphism.
 */
public class Admin extends User {

    // ── Constructors ──────────────────────────────────────────────────────────
    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(String userId, String name, String email, String password, String phone) {
        super(userId, name, email, password, phone, "ADMIN");
    }

    // ── Polymorphism ──────────────────────────────────────────────────────────

    @Override
    public String displayInfo() {
        return String.format("[ADMIN] %s (%s)", getName(), getEmail());
    }
}
