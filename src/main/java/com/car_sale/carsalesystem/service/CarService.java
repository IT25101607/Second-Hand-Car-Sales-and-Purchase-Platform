package com.car_sale.carsalesystem.service;

import com.car_sale.carsalesystem.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CarService – full CRUD backed by cars.txt (no database).
 * File format per line: carId|sellerId|brand|model|year|price|condition|description|type
 */
@Service
public class CarService {

    private static final String FILE_PATH = "cars.txt";

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves a new car listing to cars.txt.
     */
    public void addCar(Car car) {
        if (car.getCarId() == null || car.getCarId().isBlank()) {
            car.setCarId(generateId());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(car.toFileString());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not write to cars.txt", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all car listings. */
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_PATH))) return cars;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    Car c = Car.fromFileString(line);
                    if (c != null) cars.add(c);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read cars.txt", e);
        }
        return cars;
    }

    /** Finds a car by its unique ID. */
    public Car findById(String carId) {
        return getAllCars().stream()
                .filter(c -> c.getCarId().equalsIgnoreCase(carId))
                .findFirst()
                .orElse(null);
    }

    /** Returns all listings belonging to a specific seller. */
    public List<Car> findBySeller(String sellerId) {
        return getAllCars().stream()
                .filter(c -> c.getSellerId().equalsIgnoreCase(sellerId))
                .collect(Collectors.toList());
    }

    /**
     * Searches listings by brand or model (case-insensitive, partial match).
     */
    public List<Car> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllCars();
        String kw = keyword.toLowerCase();
        return getAllCars().stream()
                .filter(c -> c.getBrand().toLowerCase().contains(kw)
                          || c.getModel().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates price, condition, and/or description for an existing listing.
     *
     * @return true if found and updated.
     */
    public boolean updateCar(String carId, double newPrice,
                             String newCondition, String newDescription,
                             Integer mileage, String certificationBody, Integer warrantyMonths,
                             String imageUrl) {
        List<Car> cars = getAllCars();
        boolean found = false;
        for (Car c : cars) {
            if (c.getCarId().equalsIgnoreCase(carId)) {
                if (newPrice > 0)                              c.setPrice(newPrice);
                if (newCondition   != null && !newCondition.isBlank())   c.setCondition(newCondition);
                if (newDescription != null && !newDescription.isBlank()) c.setDescription(newDescription);
                if (imageUrl       != null && !imageUrl.isBlank())       c.setImageUrl(imageUrl);

                if (c instanceof UsedCar && mileage != null) {
                    ((UsedCar) c).setMileage(mileage);
                }
                if (c instanceof CertifiedCar) {
                    if (certificationBody != null && !certificationBody.isBlank()) {
                        ((CertifiedCar) c).setCertificationBody(certificationBody);
                    }
                    if (warrantyMonths != null) {
                        ((CertifiedCar) c).setWarrantyMonths(warrantyMonths);
                    }
                }

                found = true;
                break;
            }
        }
        if (found) rewriteFile(cars);
        return found;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Removes a listing permanently.
     *
     * @return true if found and removed.
     */
    public boolean deleteCar(String carId) {
        List<Car> cars = getAllCars();
        List<Car> filtered = cars.stream()
                .filter(c -> !c.getCarId().equalsIgnoreCase(carId))
                .collect(Collectors.toList());
        if (filtered.size() == cars.size()) return false;
        rewriteFile(filtered);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void rewriteFile(List<Car> cars) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Car c : cars) {
                bw.write(c.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not rewrite cars.txt", e);
        }
    }

    private String generateId() {
        return "C" + System.currentTimeMillis();
    }
}
