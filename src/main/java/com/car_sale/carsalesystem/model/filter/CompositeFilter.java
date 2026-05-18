package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;
import java.util.ArrayList;
import java.util.List;

/**
 * CompositeFilter – chains multiple SearchFilter objects together.
 * A car must satisfy ALL added filters to be included (AND logic).
 *
 * Demonstrates Polymorphism: it calls matches() on each filter
 * without knowing which concrete subclass each one is.
 */
public class CompositeFilter extends SearchFilter {

    private final List<SearchFilter> filters = new ArrayList<>();

    public CompositeFilter() {
        super("Composite Filter");
    }

    /** Adds a filter to the chain (fluent API). */
    public CompositeFilter add(SearchFilter filter) {
        if (filter != null) filters.add(filter);
        return this;
    }

    public List<SearchFilter> getFilters() { return filters; }

    /**
     * Polymorphic dispatch: iterates over concrete filters and calls
     * their overridden matches() implementations.
     */
    @Override
    public boolean matches(Car car) {
        for (SearchFilter f : filters) {
            if (!f.matches(car)) return false; // AND logic
        }
        return true;
    }
}
