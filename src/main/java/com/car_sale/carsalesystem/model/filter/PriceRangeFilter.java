package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;

/**
 * PriceRangeFilter – concrete SearchFilter (Polymorphism).
 * Matches cars whose price falls within [minPrice, maxPrice].
 * A value of 0 for min or max means "no bound".
 */
public class PriceRangeFilter extends SearchFilter {

    private final double minPrice;
    private final double maxPrice;

    public PriceRangeFilter(double minPrice, double maxPrice) {
        super(buildLabel(minPrice, maxPrice));
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }

    @Override
    public boolean matches(Car car) {
        if (minPrice > 0 && car.getPrice() < minPrice) return false;
        if (maxPrice > 0 && car.getPrice() > maxPrice) return false;
        return true;
    }

    private static String buildLabel(double min, double max) {
        if (min > 0 && max > 0) return String.format("Price: $%.0f – $%.0f", min, max);
        if (min > 0)             return String.format("Price: ≥ $%.0f", min);
        if (max > 0)             return String.format("Price: ≤ $%.0f", max);
        return "Price: Any";
    }
}
