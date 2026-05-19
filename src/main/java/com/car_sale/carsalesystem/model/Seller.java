package com.car_sale.carsalesystem.model;

/**
 * Seller – inherits from User (Inheritance).
 * Overrides displayInfo() to demonstrate Polymorphism.
 */
public class Seller extends User {

    private int listingCount; // number of cars listed for sale

    // ── Constructors ──────────────────────────────────────────────────────────
    public Seller() {
        super();
        setRole("SELLER");
    }

    public Seller(String userId, String name, String email,
                  String phone, String password) {
        super(userId, name, email, phone, password, "SELLER");
    }

    // ── Getter / Setter ───────────────────────────────────────────────────────
    public int  getListingCount()              { return listingCount; }
    public void setListingCount(int count)     { this.listingCount = count; }

    // ── Polymorphism: override displayInfo() ──────────────────────────────────
    @Override
    public String displayInfo() {
        return String.format("Seller[id=%s, name=%s, email=%s, listings=%d]",
                getUserId(), getName(), getEmail(), listingCount);
    }
}
