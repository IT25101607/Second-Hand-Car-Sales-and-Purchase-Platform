package com.car_sale.carsalesystem.model;

/**
 * ApprovedTransaction – represents a completed (seller-approved) deal.
 * Inherits from Transaction (Inheritance).
 * Overrides getSummary() and getStatusBadgeClass() → Polymorphism.
 */
public class ApprovedTransaction extends Transaction {

    // ── Constructors ──────────────────────────────────────────────────────────
    public ApprovedTransaction() {
        super();
        setType("APPROVED_TXN");
        setStatus(STATUS_APPROVED);
    }

    public ApprovedTransaction(String txnId, String carId, String buyerId, String sellerId,
                               double amount, String status, String createdAt, String message) {
        super(txnId, carId, buyerId, sellerId, amount, status, createdAt,
              message, "APPROVED_TXN");
    }

    // ── Polymorphism: concrete implementations of abstract methods ─────────────

    @Override
    public String getSummary() {
        return String.format(
            "Completed Deal #%s | Car: %s | Buyer: %s | Seller: %s | Amount: $%.2f",
            getTxnId(), getCarId(), getBuyerId(), getSellerId(), getAmount());
    }

    @Override
    public String getStatusBadgeClass() {
        // ApprovedTransaction is always a completed deal
        return "badge-approved";
    }
}
