package com.car_sale.carsalesystem.service;

import com.car_sale.carsalesystem.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReviewService – full CRUD backed by reviews.txt.
 *
 * OOP concepts:
 *  - Abstraction  : works through Review base type (getDisplayContent, getRatingLabel)
 *  - Polymorphism : NormalReview vs VerifiedReview behave differently via abstract methods
 */
@Service
public class ReviewService {

    private static final String FILE_PATH = "reviews.txt";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TransactionService transactionService;

    // ═══════════════════════════════════════════════════════════════════════════
    // CREATE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Adds a new review. Automatically promotes to VerifiedReview if the
     * reviewer has an APPROVED transaction for the car.
     */
    public Review addReview(String carId, String reviewerId,
                            int rating, String comment) {
        String now      = LocalDateTime.now().format(FMT);
        String reviewId = "R" + System.currentTimeMillis();

        // Polymorphism: choose correct subtype based on transaction history
        boolean verified = transactionService.getByBuyer(reviewerId).stream()
                .anyMatch(t -> t.getCarId().equalsIgnoreCase(carId)
                            && t.isApproved());

        Review review = verified
                ? new VerifiedReview(reviewId, carId, reviewerId, rating, comment, now)
                : new NormalReview(reviewId, carId, reviewerId, rating, comment, now);

        writeToFile(review);
        return review;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ
    // ═══════════════════════════════════════════════════════════════════════════

    public List<Review> getAllReviews() {
        List<Review> list = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_PATH))) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    Review r = Review.fromFileString(line);
                    if (r != null) list.add(r);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read reviews.txt", e);
        }
        return list;
    }

    /** All reviews for a specific car, newest first. */
    public List<Review> getReviewsForCar(String carId) {
        return getAllReviews().stream()
                .filter(r -> r.getCarId().equalsIgnoreCase(carId))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** All reviews written by a specific user. */
    public List<Review> getReviewsByUser(String reviewerId) {
        return getAllReviews().stream()
                .filter(r -> r.getReviewerId().equalsIgnoreCase(reviewerId))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Review findById(String reviewId) {
        return getAllReviews().stream()
                .filter(r -> r.getReviewId().equalsIgnoreCase(reviewId))
                .findFirst().orElse(null);
    }

    /** Average rating for a car (0.0 if no reviews). */
    public double getAverageRating(String carId) {
        List<Review> reviews = getReviewsForCar(carId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════════════

    public boolean updateReview(String reviewId, String reviewerId,
                                int newRating, String newComment) {
        List<Review> all = getAllReviews();
        boolean found = false;
        for (Review r : all) {
            if (r.getReviewId().equalsIgnoreCase(reviewId)
                    && r.getReviewerId().equalsIgnoreCase(reviewerId)) {
                r.setRating(newRating);
                r.setComment(newComment);
                found = true;
                break;
            }
        }
        if (found) rewriteFile(all);
        return found;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════

    public boolean deleteReview(String reviewId, String reviewerId) {
        List<Review> all = getAllReviews();
        List<Review> filtered = all.stream()
                .filter(r -> !(r.getReviewId().equalsIgnoreCase(reviewId)
                             && r.getReviewerId().equalsIgnoreCase(reviewerId)))
                .collect(Collectors.toList());
        if (filtered.size() == all.size()) return false;
        rewriteFile(filtered);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /** Check if user already reviewed this car. */
    public boolean hasReviewed(String carId, String reviewerId) {
        return getAllReviews().stream()
                .anyMatch(r -> r.getCarId().equalsIgnoreCase(carId)
                            && r.getReviewerId().equalsIgnoreCase(reviewerId));
    }

    private void writeToFile(Review r) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(r.toFileString());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not write to reviews.txt", e);
        }
    }

    private void rewriteFile(List<Review> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Review r : list) {
                bw.write(r.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not rewrite reviews.txt", e);
        }
    }
}
