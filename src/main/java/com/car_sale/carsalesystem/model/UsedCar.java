package com.car_sale.carsalesystem.model;

/**
 * UsedCar – inherits from Car (Inheritance).
 * Represents a second-hand vehicle with extra mileage info.
 * Overrides displayInfo() → Polymorphism.
 */
public class UsedCar extends Car {

    private int mileage; // in kilometres

    // ── Constructors ──────────────────────────────────────────────────────────
    public UsedCar() {
        super();
        setType("USED_CAR");
        setCondition("Used");
    }

    public UsedCar(String carId, String sellerId, String brand, String model,
                   int year, double price, String description) {
        super(carId, sellerId, brand, model, year, price, "Used", description, "USED_CAR");
    }

    // ── Getter / Setter ───────────────────────────────────────────────────────
    public int  getMileage()           { return mileage; }
    public void setMileage(int mileage){ this.mileage = mileage; }

    // ── Polymorphism: override displayInfo() ──────────────────────────────────
    @Override
    public String displayInfo() {
        return String.format("UsedCar: %s %s (%d) – $%.2f | Mileage: %,d km",
                getBrand(), getModel(), getYear(), getPrice(), mileage);
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + mileage;
    }
}
