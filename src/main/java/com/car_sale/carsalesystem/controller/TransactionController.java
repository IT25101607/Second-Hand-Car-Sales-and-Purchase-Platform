package com.car_sale.carsalesystem.controller;

import com.car_sale.carsalesystem.model.*;
import com.car_sale.carsalesystem.service.CarService;
import com.car_sale.carsalesystem.service.TransactionService;
import com.car_sale.carsalesystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CarService         carService;
    private final UserService        userService;

    public TransactionController(TransactionService transactionService,
                                 CarService carService, UserService userService) {
        this.transactionService = transactionService;
        this.carService         = carService;
        this.userService        = userService;
    }

    private User loggedIn(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUY REQUEST PAGE  (GET – show form)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/buy/{carId}")
    public String showBuyRequest(@PathVariable String carId,
                                 HttpSession session, Model model) {
        User buyer = loggedIn(session);
        if (buyer == null)                               return "redirect:/login";
        if (!"BUYER".equalsIgnoreCase(buyer.getRole())) {
            return "redirect:/cars/" + carId;           // sellers can't buy
        }

        Car car = carService.findById(carId);
        if (car == null) return "redirect:/cars";

        // Prevent buying own listing (shouldn't happen if buyer can't list, but guard anyway)
        if (car.getSellerId().equalsIgnoreCase(buyer.getUserId()))
            return "redirect:/cars/" + carId;

        User seller = userService.findById(car.getSellerId());
        model.addAttribute("car",    car);
        model.addAttribute("seller", seller);
        model.addAttribute("buyer",  buyer);
        return "transaction/buy-request";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBMIT BUY REQUEST  (POST – Create)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/buy/{carId}")
    public String submitBuyRequest(
            @PathVariable String carId,
            @RequestParam(required = false, defaultValue = "") String message,
            HttpSession session, RedirectAttributes ra) {

        User buyer = loggedIn(session);
        if (buyer == null) return "redirect:/login";

        Car car = carService.findById(carId);
        if (car == null) {
            ra.addFlashAttribute("error", "Car listing not found.");
            return "redirect:/cars";
        }

        transactionService.createPurchaseRequest(
                carId, buyer.getUserId(), car.getSellerId(), car.getPrice(), message);

        ra.addFlashAttribute("success",
                "Purchase request sent! The seller will review your request.");
        return "redirect:/transactions/history";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TRANSACTION HISTORY  (GET – Read, buyer view)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        if ("SELLER".equalsIgnoreCase(user.getRole())) {
            // Sellers see all their sale transactions
            model.addAttribute("transactions", transactionService.getBySeller(user.getUserId()));
            model.addAttribute("role", "SELLER");
        } else {
            model.addAttribute("transactions", transactionService.getByBuyer(user.getUserId()));
            model.addAttribute("role", "BUYER");
        }

        // Enrich with car data map (carId → Car)
        java.util.Map<String, Car> carMap = new java.util.HashMap<>();
        carService.getAllCars().forEach(c -> carMap.put(c.getCarId(), c));
        model.addAttribute("carMap", carMap);
        model.addAttribute("user", user);
        return "transaction/history";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SELLER APPROVAL PAGE  (GET – list pending requests)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/approvals")
    public String approvalPage(HttpSession session, Model model) {
        User seller = loggedIn(session);
        if (seller == null)                                 return "redirect:/login";
        if (!"SELLER".equalsIgnoreCase(seller.getRole()))  return "redirect:/transactions/history";

        java.util.List<Transaction> pending =
                transactionService.getPendingForSeller(seller.getUserId());

        java.util.Map<String, Car>  carMap  = new java.util.HashMap<>();
        java.util.Map<String, User> buyerMap = new java.util.HashMap<>();
        carService.getAllCars().forEach(c -> carMap.put(c.getCarId(), c));
        for (Transaction t : pending) {
            User b = userService.findById(t.getBuyerId());
            if (b != null) buyerMap.put(t.getBuyerId(), b);
        }

        model.addAttribute("pending",   pending);
        model.addAttribute("carMap",    carMap);
        model.addAttribute("buyerMap",  buyerMap);
        model.addAttribute("seller",    seller);
        return "transaction/seller-approval";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPROVE  (POST – Update status → APPROVED)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{txnId}/approve")
    public String approve(@PathVariable String txnId,
                          HttpSession session, RedirectAttributes ra) {
        User seller = loggedIn(session);
        if (seller == null) return "redirect:/login";

        boolean ok = transactionService.approveTransaction(txnId, seller.getUserId());
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Transaction approved! 🎉" : "Could not approve transaction.");
        return "redirect:/transactions/approvals";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REJECT  (POST – Update status → REJECTED)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{txnId}/reject")
    public String reject(@PathVariable String txnId,
                         HttpSession session, RedirectAttributes ra) {
        User seller = loggedIn(session);
        if (seller == null) return "redirect:/login";

        boolean ok = transactionService.rejectTransaction(txnId, seller.getUserId());
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Transaction rejected." : "Could not reject transaction.");
        return "redirect:/transactions/approvals";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE  (POST – remove from history)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{txnId}/delete")
    public String delete(@PathVariable String txnId,
                         HttpSession session, RedirectAttributes ra) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        boolean ok = transactionService.deleteTransaction(txnId, user.getUserId());
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Transaction removed from history." : "Could not delete transaction.");
        return "redirect:/transactions/history";
    }
}
