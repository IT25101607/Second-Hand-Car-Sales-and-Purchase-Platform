package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;

/**
 * TypeFilter – concrete SearchFilter (Polymorphism).
 * Matches cars by listing type: CAR | USED_CAR | CERTIFIED_CAR.
 * Empty/null type means "match all".
 */
public class TypeFilter extends SearchFilter {

    private final String type; // "CAR", "USED_CAR", "CERTIFIED_CAR", or ""

    public TypeFilter(String type) {
        super(buildLabel(type));
        this.type = (type == null) ? "" : type.trim().toUpperCase();
    }

    public String getType() { return type; }

    @Override
    public boolean matches(Car car) {
        if (type.isEmpty()) return true;
        return car.getType().equalsIgnoreCase(type);
    }

    private static String buildLabel(String type) {
        if (type == null || type.isBlank()) return "Type: Any";
        return switch (type.trim().toUpperCase()) {
            case "USED_CAR"       -> "Type: Used";
            case "CERTIFIED_CAR"  -> "Type: Certified";
            default               -> "Type: New";
        };
    }
}
