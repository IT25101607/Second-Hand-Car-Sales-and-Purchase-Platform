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
 * TransactionService – full CRUD backed by transactions.txt.
 *
 * Abstraction: all callers work through the Transaction base type.
 * Polymorphism: getSummary() / getStatusBadgeClass() dispatch to subtype.
 */
@Service
public class TransactionService {

    private static final String FILE_PATH = "transactions.txt";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CarService carService;

    // ═══════════════════════════════════════════════════════════════════════════
    // CREATE – Buyer places a purchase request
    // ═══════════════════════════════════════════════════════════════════════════

    public Transaction createPurchaseRequest(String carId, String buyerId,
                                             String sellerId, double amount,
                                             String message) {
        String txnId = "T" + System.currentTimeMillis();
        String now   = LocalDateTime.now().format(FMT);

        // Uses PurchaseRequest subtype (status = PENDING)
        PurchaseRequest req = new PurchaseRequest(
                txnId, carId, buyerId, sellerId, amount, Transaction.STATUS_PENDING, now, message);
        writeToFile(req);
        return req;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ
    // ═══════════════════════════════════════════════════════════════════════════

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_PATH))) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    Transaction t = Transaction.fromFileString(line);
                    if (t != null) list.add(t);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read transactions.txt", e);
        }
        return list;
    }

    public Transaction findById(String txnId) {
        return getAllTransactions().stream()
                .filter(t -> t.getTxnId().equalsIgnoreCase(txnId))
                .findFirst().orElse(null);
    }

    /** All transactions where the user is the buyer. */
    public List<Transaction> getByBuyer(String buyerId) {
        return getAllTransactions().stream()
                .filter(t -> t.getBuyerId().equalsIgnoreCase(buyerId))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** All transactions where the user is the seller. */
    public List<Transaction> getBySeller(String sellerId) {
        return getAllTransactions().stream()
                .filter(t -> t.getSellerId().equalsIgnoreCase(sellerId))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** Pending requests awaiting seller approval. */
    public List<Transaction> getPendingForSeller(String sellerId) {
        return getBySeller(sellerId).stream()
                .filter(Transaction::isPending)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UPDATE – Seller approves or rejects
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Approves a pending request.
     * Converts the record's type to APPROVED_TXN (Polymorphism: the object
     * stored changes from PurchaseRequest → ApprovedTransaction).
     */
    public boolean approveTransaction(String txnId, String sellerId) {
        List<Transaction> all = getAllTransactions();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            Transaction t = all.get(i);
            if (t.getTxnId().equalsIgnoreCase(txnId)
                    && t.getSellerId().equalsIgnoreCase(sellerId)
                    && t.isPending()) {
                // Upgrade to ApprovedTransaction
                ApprovedTransaction approved = new ApprovedTransaction(
                        t.getTxnId(), t.getCarId(), t.getBuyerId(), t.getSellerId(),
                        t.getAmount(), Transaction.STATUS_APPROVED,
                        t.getCreatedAt(), t.getMessage());
                all.set(i, approved);
                found = true;
                break;
            }
        }
        if (found) rewriteFile(all);
        return found;
    }

    /** Rejects a pending request (status → REJECTED, stays PurchaseRequest type). */
    public boolean rejectTransaction(String txnId, String sellerId) {
        return updateStatus(txnId, sellerId, Transaction.STATUS_REJECTED);
    }

    private boolean updateStatus(String txnId, String sellerId, String newStatus) {
        List<Transaction> all = getAllTransactions();
        boolean found = false;
        for (Transaction t : all) {
            if (t.getTxnId().equalsIgnoreCase(txnId)
                    && t.getSellerId().equalsIgnoreCase(sellerId)) {
                t.setStatus(newStatus);
                found = true;
                break;
            }
        }
        if (found) rewriteFile(all);
        return found;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE – Remove completed/cancelled transactions
    // ═══════════════════════════════════════════════════════════════════════════

    public boolean deleteTransaction(String txnId, String userId) {
        List<Transaction> all = getAllTransactions();
        List<Transaction> filtered = all.stream()
                .filter(t -> !(t.getTxnId().equalsIgnoreCase(txnId)
                        && (t.getBuyerId().equalsIgnoreCase(userId)
                            || t.getSellerId().equalsIgnoreCase(userId))))
                .collect(Collectors.toList());
        if (filtered.size() == all.size()) return false;
        rewriteFile(filtered);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private void writeToFile(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(t.toFileString());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not write to transactions.txt", e);
        }
    }

    private void rewriteFile(List<Transaction> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Transaction t : list) {
                bw.write(t.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not rewrite transactions.txt", e);
        }
    }
}
