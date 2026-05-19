package com.car_sale.carsalesystem.controller;

import com.car_sale.carsalesystem.model.*;
import com.car_sale.carsalesystem.service.CarService;
import com.car_sale.carsalesystem.service.ReviewService;
import com.car_sale.carsalesystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
@RequestMapping("/cars")
public class CarListingServlet {

    private final CarService    carService;
    private final UserService   userService;
    private final ReviewService reviewService;

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    public CarListingServlet(CarService carService, UserService userService,
                             ReviewService reviewService) {
        this.carService    = carService;
        this.userService   = userService;
        this.reviewService = reviewService;
    }

    // ── Helper: ensure user is logged in ─────────────────────────────────────
    private User getLoggedIn(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LIST / SEARCH  (all users)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping
    public String listCars(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("cars", carService.search(q));
        model.addAttribute("keyword", q == null ? "" : q);
        return "car/list";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DETAIL VIEW
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/{carId}")
    public String viewCar(@PathVariable String carId, Model model, HttpSession session) {
        Car car = carService.findById(carId);
        if (car == null) return "redirect:/cars";

        User seller = userService.findById(car.getSellerId());
        User loggedIn = getLoggedIn(session);

        model.addAttribute("car",    car);
        model.addAttribute("seller", seller);
        model.addAttribute("isOwner", loggedIn != null
                && loggedIn.getUserId().equalsIgnoreCase(car.getSellerId()));
        // Show Buy button only for logged-in buyers who don't own this listing
        model.addAttribute("isBuyer", loggedIn != null
                && "BUYER".equalsIgnoreCase(loggedIn.getRole())
                && !loggedIn.getUserId().equalsIgnoreCase(car.getSellerId()));
        // Review stats
        model.addAttribute("reviewCount", reviewService.getReviewsForCar(car.getCarId()).size());
        model.addAttribute("avgRating",   reviewService.getAverageRating(car.getCarId()));
        return "car/detail";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADD LISTING  (Sellers only)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";
        if (!"SELLER".equalsIgnoreCase(user.getRole())) {
            model.addAttribute("error", "Only sellers can add car listings.");
            return "redirect:/cars";
        }
        return "car/add";
    }

    @PostMapping("/add")
    public String processAdd(
            @RequestParam String brand,
            @RequestParam String model2,
            @RequestParam int    year,
            @RequestParam double price,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam(required = false) String        imageUrl,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) String  certificationBody,
            @RequestParam(required = false) Integer warrantyMonths,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";

        Car car;
        switch (type.toUpperCase()) {
            case "USED_CAR":
                UsedCar uc = new UsedCar(null, user.getUserId(), brand, model2, year, price, description);
                if (mileage != null) uc.setMileage(mileage);
                car = uc;
                break;
            case "CERTIFIED_CAR":
                CertifiedCar cc = new CertifiedCar(null, user.getUserId(), brand, model2, year, price, description);
                if (certificationBody != null && !certificationBody.isBlank()) cc.setCertificationBody(certificationBody);
                if (warrantyMonths != null) cc.setWarrantyMonths(warrantyMonths);
                car = cc;
                break;
            default:
                car = new Car(null, user.getUserId(), brand, model2, year, price, "New", description, "CAR");
        }

        // ── Save uploaded image file (priority over URL) ──
        String resolvedUrl = resolveImageUrl(imageFile, imageUrl, ra);
        if (resolvedUrl != null && !resolvedUrl.isBlank()) car.setImageUrl(resolvedUrl);

        carService.addCar(car);
        ra.addFlashAttribute("success", "Car listing added successfully!");
        return "redirect:/cars";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDIT LISTING
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/{carId}/edit")
    public String showEditForm(@PathVariable String carId,
                               HttpSession session, Model model) {
        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";

        Car car = carService.findById(carId);
        if (car == null || !car.getSellerId().equalsIgnoreCase(user.getUserId()))
            return "redirect:/cars";

        model.addAttribute("car", car);
        return "car/edit";
    }

    @PostMapping("/{carId}/edit")
    public String processEdit(
            @PathVariable String carId,
            @RequestParam double price,
            @RequestParam String condition,
            @RequestParam String description,
            @RequestParam(required = false) String        imageUrl,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) String  certificationBody,
            @RequestParam(required = false) Integer warrantyMonths,
            HttpSession session,
            RedirectAttributes ra) {

        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";

        Car car = carService.findById(carId);
        if (car == null || !car.getSellerId().equalsIgnoreCase(user.getUserId())) {
            ra.addFlashAttribute("error", "Not authorised to edit this listing.");
            return "redirect:/cars";
        }

        // ── Save uploaded image (priority over URL) ──
        String resolvedUrl = resolveImageUrl(imageFile, imageUrl, ra);
        if (resolvedUrl == null || resolvedUrl.isBlank()) resolvedUrl = car.getImageUrl(); // keep existing

        boolean ok = carService.updateCar(carId, price, condition, description,
                mileage, certificationBody, warrantyMonths, resolvedUrl);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Listing updated!" : "Update failed. Car not found.");
        return "redirect:/cars/" + carId;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE LISTING
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{carId}/delete")
    public String deleteListing(@PathVariable String carId,
                                HttpSession session, RedirectAttributes ra) {
        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";

        Car car = carService.findById(carId);
        if (car != null && car.getSellerId().equalsIgnoreCase(user.getUserId())) {
            carService.deleteCar(carId);
            ra.addFlashAttribute("success", "Listing removed.");
        } else {
            ra.addFlashAttribute("error", "Not authorised to delete this listing.");
        }
        return "redirect:/cars";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MY LISTINGS (Seller's own cars)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/my")
    public String myListings(HttpSession session, Model model) {
        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("cars", carService.findBySeller(user.getUserId()));
        model.addAttribute("user", user);
        return "car/my-listings";
    }

    // ── Private helper: save uploaded file OR fall back to URL ─────────────────
    private String resolveImageUrl(MultipartFile imageFile, String imageUrl, RedirectAttributes ra) {
        // Priority 1: uploaded file
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                Files.createDirectories(uploadPath);

                String original = imageFile.getOriginalFilename();
                String ext = (original != null && original.contains("."))
                        ? original.substring(original.lastIndexOf('.'))
                        : ".jpg";
                String filename = UUID.randomUUID().toString() + ext;

                Files.copy(imageFile.getInputStream(),
                        uploadPath.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);

                return "/uploads/" + filename;
            } catch (IOException e) {
                ra.addFlashAttribute("warning", "Image upload failed, listing saved without image.");
                return "";
            }
        }
        // Priority 2: URL typed manually
        if (imageUrl != null && !imageUrl.isBlank()) return imageUrl;
        return "";
    }
}
