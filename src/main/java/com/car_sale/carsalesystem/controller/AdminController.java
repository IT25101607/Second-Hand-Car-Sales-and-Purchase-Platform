package com.car_sale.carsalesystem.controller;

import com.car_sale.carsalesystem.model.User;
import com.car_sale.carsalesystem.service.CarService;
import com.car_sale.carsalesystem.service.ReviewService;
import com.car_sale.carsalesystem.service.TransactionService;
import com.car_sale.carsalesystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CarService carService;
    private final TransactionService transactionService;
    private final ReviewService reviewService;

    public AdminController(UserService userService, CarService carService,
                           TransactionService transactionService, ReviewService reviewService) {
        this.userService = userService;
        this.carService = carService;
        this.transactionService = transactionService;
        this.reviewService = reviewService;
    }

    private User getAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return user;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User admin = getAdmin(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("admin", admin);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("cars", carService.getAllCars());
        model.addAttribute("transactions", transactionService.getAllTransactions());
        model.addAttribute("reviews", reviewService.getAllReviews());

        return "admin/dashboard";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE USER
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable String userId, HttpSession session, RedirectAttributes ra) {
        if (getAdmin(session) == null) return "redirect:/login";

        User u = userService.findById(userId);
        if (u != null && "ADMIN".equalsIgnoreCase(u.getRole())) {
            ra.addFlashAttribute("error", "Cannot delete another administrator.");
            return "redirect:/admin/dashboard";
        }

        boolean ok = userService.deleteUser(userId);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "User deleted successfully." : "Failed to delete user.");
        return "redirect:/admin/dashboard";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE CAR LISTING
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/cars/{carId}/delete")
    public String deleteCar(@PathVariable String carId, HttpSession session, RedirectAttributes ra) {
        if (getAdmin(session) == null) return "redirect:/login";

        boolean ok = carService.deleteCar(carId);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Car listing removed." : "Failed to remove car listing.");
        return "redirect:/admin/dashboard";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE REVIEW
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable String reviewId, @RequestParam String reviewerId, 
                               HttpSession session, RedirectAttributes ra) {
        if (getAdmin(session) == null) return "redirect:/login";

        boolean ok = reviewService.deleteReview(reviewId, reviewerId);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Review removed." : "Failed to remove review.");
        return "redirect:/admin/dashboard";
    }
}
