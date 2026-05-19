package com.car_sale.carsalesystem.model;

/**
 * Review – abstract base class demonstrating:
 *   • Encapsulation : all fields private with getters/setters
 *   • Abstraction   : getDisplayContent() and getRatingLabel() are abstract
 *
 * File format (reviews.txt):
 *   reviewId|carId|reviewerId|rating|comment|createdAt|type
 *
 * type values: "NORMAL" | "VERIFIED"
 * VERIFIED = reviewer completed a transaction for this car.
 */
public abstract class Review {

    // ── Private fields (Encapsulation) ────────────────────────────────────────
    private String reviewId;
    private String carId;
    private String reviewerId;   // userId of the reviewer
    private int    rating;       // 1–5
    private String comment;
    private String createdAt;    // yyyy-MM-dd HH:mm:ss
    private String type;         // "NORMAL" | "VERIFIED"

    // ── Constructors ──────────────────────────────────────────────────────────
    public Review() {}

    public Review(String reviewId, String carId, String reviewerId,
                  int rating, String comment, String createdAt, String type) {
        this.reviewId   = reviewId;
        this.carId      = carId;
        this.reviewerId = reviewerId;
        this.rating     = rating;
        this.comment    = comment;
        this.createdAt  = createdAt;
        this.type       = type;
    }

    // ── Abstract methods (Abstraction + Polymorphism) ─────────────────────────

    /**
     * Returns a human-readable formatted content string for display.
     * Each subclass renders it differently (Polymorphism).
     */
    public abstract String getDisplayContent();

    /**
     * Returns the star label for this rating (e.g. "⭐⭐⭐").
     * Each subclass can format the label differently.
     */
    public abstract String getRatingLabel();

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getReviewId()                  { return reviewId; }
    public void   setReviewId(String reviewId)   { this.reviewId = reviewId; }

    public String getCarId()               { return carId; }
    public void   setCarId(String carId)   { this.carId = carId; }

    public String getReviewerId()                  { return reviewerId; }
    public void   setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public int  getRating()            { return rating; }
    public void setRating(int rating)  { this.rating = rating; }

    public String getComment()               { return comment; }
    public void   setComment(String comment) { this.comment = comment; }

    public String getCreatedAt()               { return createdAt; }
    public void   setCreatedAt(String ts)      { this.createdAt = ts; }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    // ── Convenience ───────────────────────────────────────────────────────────
    public boolean isVerified() { return "VERIFIED".equalsIgnoreCase(type); }

    /** Generates a star string: ★★★☆☆ for rating=3 */
    public static String buildStars(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= rating ? "★" : "☆");
        return sb.toString();
    }

    // ── File serialisation ────────────────────────────────────────────────────
    public String toFileString() {
        return String.join("|",
                reviewId, carId, reviewerId,
                String.valueOf(rating),
                comment == null ? "" : comment.replace("|", ";"),
                createdAt, type);
    }

    public static Review fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 7) return null;
        String reviewId  = p[0];
        String carId     = p[1];
        String reviewerId= p[2];
        int    rating    = parseInt(p[3]);
        String comment   = p[4];
        String createdAt = p[5];
        String type      = p[6];

        if ("VERIFIED".equalsIgnoreCase(type)) {
            return new VerifiedReview(reviewId, carId, reviewerId, rating, comment, createdAt);
        } else {
            return new NormalReview(reviewId, carId, reviewerId, rating, comment, createdAt);
        }
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
