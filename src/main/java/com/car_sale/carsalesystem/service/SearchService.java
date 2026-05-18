package com.car_sale.carsalesystem.service;

import com.car_sale.carsalesystem.model.Car;
import com.car_sale.carsalesystem.model.SearchPreference;
import com.car_sale.carsalesystem.model.filter.*;
// KeywordFilter, BrandFilter, PriceRangeFilter, YearFilter, TypeFilter, CompositeFilter
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SearchService – orchestrates filter-based car search and
 * manages saved search preferences (search_prefs.txt).
 *
 * OOP concepts:
 *  - Abstraction : calls SearchFilter.matches() without knowing the subtype.
 *  - Polymorphism: CompositeFilter dispatches to concrete filter implementations.
 */
@Service
public class SearchService {

    private static final String PREFS_FILE = "search_prefs.txt";

    @Autowired
    private CarService carService;

    // ═══════════════════════════════════════════════════════════════════════════
    // SEARCH  (Read / Update filters dynamically)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Executes a multi-criteria search using the filter chain.
     * Polymorphism: CompositeFilter.matches() dispatches to each
     * concrete filter's overridden implementation.
     */
    public List<Car> search(String keyword, String brand,
                            double minPrice, double maxPrice,
                            int fromYear, int toYear, String type) {

        // Build composite filter (Abstraction + Polymorphism in action)
        CompositeFilter composite = new CompositeFilter();
        composite.add(new KeywordFilter(keyword));     // free-text: brand, model, description
        composite.add(new BrandFilter(brand));         // explicit brand dropdown
        composite.add(new PriceRangeFilter(minPrice, maxPrice));
        composite.add(new YearFilter(fromYear, toYear));
        composite.add(new TypeFilter(type));

        List<Car> all = carService.getAllCars();
        return composite.apply(all);
    }

    /**
     * Returns the active filters as a readable list of labels
     * (used in the UI to display "Applied Filters" chips).
     */
    public List<String> describeFilters(String keyword, String brand,
                                        double minPrice, double maxPrice,
                                        int fromYear, int toYear, String type) {
        List<String> labels = new ArrayList<>();
        if (keyword  != null && !keyword.isBlank())  labels.add("Search: \"" + keyword + "\"");
        if (brand    != null && !brand.isBlank())    labels.add("Brand: " + brand);
        if (minPrice > 0 || maxPrice > 0)
            labels.add(new PriceRangeFilter(minPrice, maxPrice).getLabel());
        if (fromYear > 0 || toYear > 0)
            labels.add(new YearFilter(fromYear, toYear).getLabel());
        if (type     != null && !type.isBlank())     labels.add(new TypeFilter(type).getLabel());
        return labels;
    }

    /** Returns distinct brands from all listings (for the brand dropdown). */
    public List<String> getDistinctBrands() {
        return carService.getAllCars().stream()
                .map(Car::getBrand)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SAVED PREFERENCES  (Create / Read / Delete)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Saves a search preference for the logged-in user (Create). */
    public SearchPreference savePreference(SearchPreference pref) {
        if (pref.getPrefId() == null || pref.getPrefId().isBlank()) {
            pref.setPrefId("SP" + System.currentTimeMillis());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PREFS_FILE, true))) {
            bw.write(pref.toFileString());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not write to search_prefs.txt", e);
        }
        return pref;
    }

    /** Loads all saved preferences for a specific user (Read). */
    public List<SearchPreference> getPreferencesForUser(String userId) {
        List<SearchPreference> prefs = new ArrayList<>();
        if (!Files.exists(Paths.get(PREFS_FILE))) return prefs;
        try (BufferedReader br = new BufferedReader(new FileReader(PREFS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    SearchPreference sp = SearchPreference.fromFileString(line);
                    if (sp != null && sp.getUserId().equalsIgnoreCase(userId)) {
                        prefs.add(sp);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read search_prefs.txt", e);
        }
        return prefs;
    }

    /** Reads all preferences from file. */
    private List<SearchPreference> getAllPreferences() {
        List<SearchPreference> prefs = new ArrayList<>();
        if (!Files.exists(Paths.get(PREFS_FILE))) return prefs;
        try (BufferedReader br = new BufferedReader(new FileReader(PREFS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    SearchPreference sp = SearchPreference.fromFileString(line);
                    if (sp != null) prefs.add(sp);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read search_prefs.txt", e);
        }
        return prefs;
    }

    /** Deletes a saved preference by ID (Delete). */
    public boolean deletePreference(String prefId) {
        List<SearchPreference> all = getAllPreferences();
        List<SearchPreference> filtered = all.stream()
                .filter(p -> !p.getPrefId().equalsIgnoreCase(prefId))
                .collect(Collectors.toList());
        if (filtered.size() == all.size()) return false;
        rewritePrefsFile(filtered);
        return true;
    }

    /** Clears ALL saved searches for a user (Delete all). */
    public void clearPreferencesForUser(String userId) {
        List<SearchPreference> filtered = getAllPreferences().stream()
                .filter(p -> !p.getUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
        rewritePrefsFile(filtered);
    }

    private void rewritePrefsFile(List<SearchPreference> prefs) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PREFS_FILE, false))) {
            for (SearchPreference p : prefs) {
                bw.write(p.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not rewrite search_prefs.txt", e);
        }
    }
}
