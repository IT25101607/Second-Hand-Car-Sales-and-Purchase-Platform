package com.car_sale.carsalesystem.model;

/**
 * Buyer – inherits from User (Inheritance).
 * Overrides displayInfo() to demonstrate Polymorphism.
 */
public class Buyer extends User {

    private int purchaseCount; // number of cars purchased

    // ── Constructors ──────────────────────────────────────────────────────────
    public Buyer() {
        super();
        setRole("BUYER");
    }

    public Buyer(String userId, String name, String email,
                 String phone, String password) {
        super(userId, name, email, phone, password, "BUYER");
    }

    // ── Getter / Setter ───────────────────────────────────────────────────────
    public int  getPurchaseCount()               { return purchaseCount; }
    public void setPurchaseCount(int count)      { this.purchaseCount = count; }

    // ── Polymorphism: override displayInfo() ──────────────────────────────────
    @Override
    public String displayInfo() {
        return String.format("Buyer[id=%s, name=%s, email=%s, purchases=%d]",
                getUserId(), getName(), getEmail(), purchaseCount);
    }
}
