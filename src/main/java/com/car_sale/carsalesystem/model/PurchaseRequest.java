package com.car_sale.carsalesystem.model;

/**
 * PurchaseRequest – a buyer's request to purchase a car (status: PENDING initially).
 * Inherits from Transaction (Inheritance).
 * Overrides getSummary() and getStatusBadgeClass() → Polymorphism.
 */
public class PurchaseRequest extends Transaction {

    // ── Constructors ──────────────────────────────────────────────────────────
    public PurchaseRequest() {
        super();
        setType("PURCHASE_REQUEST");
        setStatus(STATUS_PENDING);
    }

    public PurchaseRequest(String txnId, String carId, String buyerId, String sellerId,
                           double amount, String status, String createdAt, String message) {
        super(txnId, carId, buyerId, sellerId, amount, status, createdAt,
              message, "PURCHASE_REQUEST");
    }

    // ── Polymorphism: concrete implementations of abstract methods ─────────────

    @Override
    public String getSummary() {
        return String.format(
            "Purchase Request #%s | Car: %s | Buyer: %s | Amount: $%.2f | Status: %s",
            getTxnId(), getCarId(), getBuyerId(), getAmount(), getStatus());
    }

    @Override
    public String getStatusBadgeClass() {
        return switch (getStatus()) {
            case STATUS_APPROVED -> "badge-approved";
            case STATUS_REJECTED -> "badge-rejected";
            default              -> "badge-pending";
        };
    }
}
