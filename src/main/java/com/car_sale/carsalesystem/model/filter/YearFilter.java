package com.car_sale.carsalesystem.model.filter;

import com.car_sale.carsalesystem.model.Car;

/**
 * YearFilter – concrete SearchFilter (Polymorphism).
 * Matches cars manufactured within [fromYear, toYear].
 * A value of 0 means "no bound".
 */
public class YearFilter extends SearchFilter {

    private final int fromYear;
    private final int toYear;

    public YearFilter(int fromYear, int toYear) {
        super(buildLabel(fromYear, toYear));
        this.fromYear = fromYear;
        this.toYear   = toYear;
    }

    public int getFromYear() { return fromYear; }
    public int getToYear()   { return toYear; }

    @Override
    public boolean matches(Car car) {
        if (fromYear > 0 && car.getYear() < fromYear) return false;
        if (toYear   > 0 && car.getYear() > toYear)   return false;
        return true;
    }

    private static String buildLabel(int from, int to) {
        if (from > 0 && to > 0) return "Year: " + from + " – " + to;
        if (from > 0)            return "Year: ≥ " + from;
        if (to   > 0)            return "Year: ≤ " + to;
        return "Year: Any";
    }
}
