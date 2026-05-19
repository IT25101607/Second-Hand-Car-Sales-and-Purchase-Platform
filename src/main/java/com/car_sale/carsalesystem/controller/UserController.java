package com.car_sale.carsalesystem.controller;

import com.car_sale.carsalesystem.model.Buyer;
import com.car_sale.carsalesystem.model.Seller;
import com.car_sale.carsalesystem.model.User;
import com.car_sale.carsalesystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── Home mapping removed to avoid conflict with IndexController ──

    // ═══════════════════════════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes ra) {

        User user = "SELLER".equalsIgnoreCase(role)
                ? new Seller(null, name, email, phone, password)
                : new Buyer(null, name, email, phone, password);

        boolean ok = userService.registerUser(user);
        if (!ok) {
            ra.addFlashAttribute("error", "Email already registered. Please log in.");
            return "redirect:/register";
        }
        ra.addFlashAttribute("success", "Account created! Please log in.");
        return "redirect:/login";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/login")
    public String showLogin() {
        return "user/login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra) {

        User user = userService.authenticate(email, password);
        if (user == null) {
            ra.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/login";
        }
        session.setAttribute("loggedInUser", user);
        return "redirect:/profile";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROFILE (Read)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        // Refresh from file to get latest data
        User fresh = userService.findById(user.getUserId());
        if (fresh != null) {
            session.setAttribute("loggedInUser", fresh);
            model.addAttribute("user", fresh);
        } else {
            model.addAttribute("user", user);
        }
        return "user/profile";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDIT PROFILE (Update)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String processEditProfile(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam(required = false) String password,
            HttpSession session,
            RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        boolean ok = userService.updateUser(user.getUserId(), name, phone, password);
        if (ok) {
            // Refresh session with updated data
            User updated = userService.findById(user.getUserId());
            session.setAttribute("loggedInUser", updated);
            ra.addFlashAttribute("success", "Profile updated successfully!");
        } else {
            ra.addFlashAttribute("error", "Update failed. Please try again.");
        }
        return "redirect:/profile";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE ACCOUNT
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/profile/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        userService.deleteUser(user.getUserId());
        session.invalidate();
        ra.addFlashAttribute("success", "Your account has been deleted.");
        return "redirect:/login";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOGOUT
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("success", "Logged out successfully.");
        return "redirect:/login";
    }
}
