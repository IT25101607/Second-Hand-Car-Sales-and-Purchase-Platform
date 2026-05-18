package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;

/**
 * KeywordFilter – concrete SearchFilter (Polymorphism).
 * Matches cars where brand OR model contains the keyword (case-insensitive).
 * Used for the general free-text search input.
 */
public class KeywordFilter extends SearchFilter {

    private final String keyword;

    public KeywordFilter(String keyword) {
        super("Search: \"" + keyword + "\"");
        this.keyword = keyword == null ? "" : keyword.trim().toLowerCase();
    }

    public String getKeyword() { return keyword; }

    @Override
    public boolean matches(Car car) {
        if (keyword.isEmpty()) return true;
        return car.getBrand().toLowerCase().contains(keyword)
            || car.getModel().toLowerCase().contains(keyword)
            || car.getDescription().toLowerCase().contains(keyword);
    }
}
