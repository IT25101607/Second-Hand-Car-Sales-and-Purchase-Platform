package com.car_sale.carsalesystem.controller;

import com.car_sale.carsalesystem.model.*;
import com.car_sale.carsalesystem.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewServlet {

    private final ReviewService reviewService;
    private final CarService    carService;
    private final UserService   userService;

    public ReviewServlet(ReviewService reviewService,
                         CarService carService, UserService userService) {
        this.reviewService = reviewService;
        this.carService    = carService;
        this.userService   = userService;
    }

    private User loggedIn(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPLAY  – list all reviews for a car
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/car/{carId}")
    public String listReviews(@PathVariable String carId,
                              HttpSession session, Model model) {
        Car  car  = carService.findById(carId);
        if (car == null) return "redirect:/cars";

        User user = loggedIn(session);
        java.util.List<Review> reviews = reviewService.getReviewsForCar(carId);

        // Enrich reviewer names
        java.util.Map<String, String> reviewerNames = new java.util.HashMap<>();
        for (Review r : reviews) {
            User reviewer = userService.findById(r.getReviewerId());
            reviewerNames.put(r.getReviewerId(),
                    reviewer != null ? reviewer.getName() : "Unknown");
        }

        model.addAttribute("car",          car);
        model.addAttribute("reviews",      reviews);
        model.addAttribute("reviewerNames",reviewerNames);
        model.addAttribute("avgRating",    reviewService.getAverageRating(carId));
        model.addAttribute("reviewCount",  reviews.size());
        model.addAttribute("canReview",    user != null && !reviewService.hasReviewed(carId, user.getUserId()));
        model.addAttribute("loggedInUser", user);
        return "review/list";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBMIT  – show form
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/car/{carId}/submit")
    public String showSubmitForm(@PathVariable String carId,
                                 HttpSession session, Model model) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        if (reviewService.hasReviewed(carId, user.getUserId())) {
            return "redirect:/reviews/car/" + carId;
        }

        Car car = carService.findById(carId);
        if (car == null) return "redirect:/cars";

        model.addAttribute("car", car);
        return "review/submit";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBMIT  – process form (Create)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/car/{carId}/submit")
    public String processSubmit(@PathVariable String carId,
                                @RequestParam int rating,
                                @RequestParam String comment,
                                HttpSession session, RedirectAttributes ra) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        if (rating < 1 || rating > 5) {
            ra.addFlashAttribute("error", "Rating must be between 1 and 5.");
            return "redirect:/reviews/car/" + carId + "/submit";
        }

        Review review = reviewService.addReview(carId, user.getUserId(), rating, comment);
        ra.addFlashAttribute("success",
                review.isVerified()
                        ? "✔ Verified review submitted!"
                        : "Review submitted successfully!");
        return "redirect:/reviews/car/" + carId;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDIT  – show form (Update)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/{reviewId}/edit")
    public String showEditForm(@PathVariable String reviewId,
                               HttpSession session, Model model) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        Review review = reviewService.findById(reviewId);
        if (review == null || !review.getReviewerId().equalsIgnoreCase(user.getUserId()))
            return "redirect:/cars";

        Car car = carService.findById(review.getCarId());
        model.addAttribute("review", review);
        model.addAttribute("car", car);
        return "review/edit";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDIT  – process form (Update)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{reviewId}/edit")
    public String processEdit(@PathVariable String reviewId,
                              @RequestParam int rating,
                              @RequestParam String comment,
                              HttpSession session, RedirectAttributes ra) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        Review review = reviewService.findById(reviewId);
        if (review == null) return "redirect:/cars";

        boolean ok = reviewService.updateReview(reviewId, user.getUserId(), rating, comment);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Review updated!" : "Could not update review.");
        return "redirect:/reviews/car/" + (review != null ? review.getCarId() : "");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{reviewId}/delete")
    public String deleteReview(@PathVariable String reviewId,
                               HttpSession session, RedirectAttributes ra) {
        User user = loggedIn(session);
        if (user == null) return "redirect:/login";

        Review review = reviewService.findById(reviewId);
        String carId = review != null ? review.getCarId() : "";

        boolean ok = reviewService.deleteReview(reviewId, user.getUserId());
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Review deleted." : "Could not delete review.");
        return "redirect:/reviews/car/" + carId;
    }
}
