package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;

/**
 * BrandFilter – concrete SearchFilter (Polymorphism).
 * Matches cars whose brand contains the keyword (case-insensitive).
 */
public class BrandFilter extends SearchFilter {

    private final String keyword;

    public BrandFilter(String keyword) {
        super("Brand: " + keyword);
        this.keyword = keyword == null ? "" : keyword.trim().toLowerCase();
    }

    public String getKeyword() { return keyword; }

    @Override
    public boolean matches(Car car) {
        if (keyword.isEmpty()) return true;
        return car.getBrand().toLowerCase().contains(keyword);
    }
}
