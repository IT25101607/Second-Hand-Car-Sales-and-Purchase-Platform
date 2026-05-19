package com.car_sale.carsalesystem.model;

/**
 * NormalReview – a standard review left by any registered user.
 * Inherits from Review (Inheritance).
 * Overrides getDisplayContent() and getRatingLabel() → Polymorphism.
 */
public class NormalReview extends Review {

    public NormalReview() {
        super();
        setType("NORMAL");
    }

    public NormalReview(String reviewId, String carId, String reviewerId,
                        int rating, String comment, String createdAt) {
        super(reviewId, carId, reviewerId, rating, comment, createdAt, "NORMAL");
    }

    // ── Polymorphism: concrete implementations ────────────────────────────────

    @Override
    public String getDisplayContent() {
        return String.format("[Review] %s | %s | %s",
                buildStars(getRating()), getComment(), getCreatedAt());
    }

    @Override
    public String getRatingLabel() {
        return buildStars(getRating()) + " (" + getRating() + "/5)";
    }
}
