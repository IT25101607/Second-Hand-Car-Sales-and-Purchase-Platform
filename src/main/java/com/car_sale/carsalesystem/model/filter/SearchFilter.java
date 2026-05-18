package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;

/**
 * SearchFilter – abstract base class demonstrating ABSTRACTION.
 *
 * Every concrete filter must implement matches() to decide
 * whether a given car satisfies the filter criterion.
 * This allows the search engine to treat all filter types
 * uniformly (Polymorphism).
 */
public abstract class SearchFilter {

    /** Human-readable label shown in the UI (e.g. "Brand: Toyota"). */
    private String label;

    public SearchFilter(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    /**
     * Abstract method – each subclass defines its own matching logic.
     * Demonstrates Abstraction: the caller only needs to call matches()
     * without knowing the internal implementation.
     *
     * @param car the car listing to test
     * @return true if the car satisfies this filter
     */
    public abstract boolean matches(Car car);

    /**
     * Convenience: filters a list by this criterion.
     */
    public java.util.List<Car> apply(java.util.List<Car> cars) {
        java.util.List<Car> result = new java.util.ArrayList<>();
        for (Car car : cars) {
            if (matches(car)) result.add(car);
        }
        return result;
    }
}
