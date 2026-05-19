package com.car_sale.carsalesystem.model;

/**
 * Transaction – abstract base class demonstrating:
 *   • Encapsulation : all fields private with getters/setters
 *   • Abstraction   : getSummary() and processStatus() are abstract;
 *                     callers work through this type without knowing the subtype
 *
 * File format (transactions.txt):
 *   txnId|carId|buyerId|sellerId|amount|status|createdAt|message
 *
 * Status lifecycle:  PENDING → APPROVED | REJECTED
 */
public abstract class Transaction {

    // ── Status constants ──────────────────────────────────────────────────────
    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    // ── Private fields (Encapsulation) ────────────────────────────────────────
    private String txnId;
    private String carId;
    private String buyerId;
    private String sellerId;
    private double amount;
    private String status;
    private String createdAt;   // ISO-8601 string
    private String message;     // buyer's message to seller
    private String type;        // "PURCHASE_REQUEST" | "APPROVED_TXN"

    // ── Constructors ──────────────────────────────────────────────────────────
    public Transaction() {}

    public Transaction(String txnId, String carId, String buyerId, String sellerId,
                       double amount, String status, String createdAt,
                       String message, String type) {
        this.txnId     = txnId;
        this.carId     = carId;
        this.buyerId   = buyerId;
        this.sellerId  = sellerId;
        this.amount    = amount;
        this.status    = status;
        this.createdAt = createdAt;
        this.message   = message;
        this.type      = type;
    }

    // ── Abstract methods (Abstraction + Polymorphism) ─────────────────────────
    /**
     * Returns a human-readable summary of this transaction.
     * Each subclass formats it differently.
     */
    public abstract String getSummary();

    /**
     * Returns the CSS badge class for the status chip in the UI.
     * Subclasses map their statuses to different visual styles.
     */
    public abstract String getStatusBadgeClass();

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getTxnId()               { return txnId; }
    public void   setTxnId(String txnId)   { this.txnId = txnId; }

    public String getCarId()               { return carId; }
    public void   setCarId(String carId)   { this.carId = carId; }

    public String getBuyerId()               { return buyerId; }
    public void   setBuyerId(String id)      { this.buyerId = id; }

    public String getSellerId()              { return sellerId; }
    public void   setSellerId(String id)     { this.sellerId = id; }

    public double getAmount()                { return amount; }
    public void   setAmount(double amount)   { this.amount = amount; }

    public String getStatus()                { return status; }
    public void   setStatus(String status)   { this.status = status; }

    public String getCreatedAt()               { return createdAt; }
    public void   setCreatedAt(String ts)      { this.createdAt = ts; }

    public String getMessage()               { return message; }
    public void   setMessage(String msg)     { this.message = msg; }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    // ── Convenience ───────────────────────────────────────────────────────────
    public boolean isPending()  { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }

    // ── File serialisation ────────────────────────────────────────────────────
    public String toFileString() {
        return String.join("|",
                txnId, carId, buyerId, sellerId,
                String.valueOf(amount), status, createdAt,
                message == null ? "" : message.replace("|", ";"),
                type);
    }

    /** Factory: deserialises a line from transactions.txt into the correct subtype. */
    public static Transaction fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 9) return null;

        String txnId    = p[0];
        String carId    = p[1];
        String buyerId  = p[2];
        String sellerId = p[3];
        double amount   = parseDouble(p[4]);
        String status   = p[5];
        String created  = p[6];
        String message  = p[7];
        String type     = p[8];

        if ("APPROVED_TXN".equals(type)) {
            return new ApprovedTransaction(txnId, carId, buyerId, sellerId,
                                           amount, status, created, message);
        } else {
            return new PurchaseRequest(txnId, carId, buyerId, sellerId,
                                       amount, status, created, message);
        }
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }
}
