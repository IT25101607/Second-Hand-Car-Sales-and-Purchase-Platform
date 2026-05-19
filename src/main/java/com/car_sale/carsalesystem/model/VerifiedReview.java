package com.car_sale.carsalesystem.model;

/**
 * VerifiedReview – a review left by a buyer who completed a transaction for this car.
 * Inherits from Review (Inheritance).
 * Overrides getDisplayContent() and getRatingLabel() → Polymorphism.
 * Adds a "✔ Verified Purchase" badge to the display.
 */
public class VerifiedReview extends Review {

    public VerifiedReview() {
        super();
        setType("VERIFIED");
    }

    public VerifiedReview(String reviewId, String carId, String reviewerId,
                          int rating, String comment, String createdAt) {
        super(reviewId, carId, reviewerId, rating, comment, createdAt, "VERIFIED");
    }

    // ── Polymorphism: concrete implementations ────────────────────────────────

    @Override
    public String getDisplayContent() {
        return String.format("[✔ Verified Purchase] %s | %s | %s",
                buildStars(getRating()), getComment(), getCreatedAt());
    }

    @Override
    public String getRatingLabel() {
        return "✔ " + buildStars(getRating()) + " (" + getRating() + "/5) – Verified Buyer";
    }
}
