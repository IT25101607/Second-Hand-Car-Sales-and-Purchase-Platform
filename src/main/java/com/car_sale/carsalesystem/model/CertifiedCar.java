package com.car_sale.carsalesystem.model;

/**
 * CertifiedCar – inherits from Car (Inheritance).
 * Represents a manufacturer-certified pre-owned vehicle.
 * Overrides displayInfo() → Polymorphism.
 */
public class CertifiedCar extends Car {

    private String certificationBody; // e.g. "Toyota Certified", "Honda True Used"
    private int    warrantyMonths;

    // ── Constructors ──────────────────────────────────────────────────────────
    public CertifiedCar() {
        super();
        setType("CERTIFIED_CAR");
        setCondition("Certified");
    }

    public CertifiedCar(String carId, String sellerId, String brand, String model,
                        int year, double price, String description) {
        super(carId, sellerId, brand, model, year, price, "Certified", description, "CERTIFIED_CAR");
        this.certificationBody = brand + " Certified";
        this.warrantyMonths    = 12;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getCertificationBody()                       { return certificationBody; }
    public void   setCertificationBody(String certBody)       { this.certificationBody = certBody; }

    public int  getWarrantyMonths()                { return warrantyMonths; }
    public void setWarrantyMonths(int months)      { this.warrantyMonths = months; }

    // ── Polymorphism: override displayInfo() ──────────────────────────────────
    @Override
    public String displayInfo() {
        return String.format("Certified: %s %s (%d) – $%.2f | %s | Warranty: %d months",
                getBrand(), getModel(), getYear(), getPrice(),
                certificationBody, warrantyMonths);
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + certificationBody + "|" + warrantyMonths;
    }
}
