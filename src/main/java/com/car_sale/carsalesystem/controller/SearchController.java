package com.car_sale.carsalesystem.controller;

import com.car_sale.carsalesystem.model.SearchPreference;
import com.car_sale.carsalesystem.model.User;
import com.car_sale.carsalesystem.service.SearchService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────
    private User getLoggedIn(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEARCH PAGE  (GET – show form + results)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping
    public String searchPage(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String brand,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false, defaultValue = "0") double maxPrice,
            @RequestParam(required = false, defaultValue = "0") int fromYear,
            @RequestParam(required = false, defaultValue = "0") int toYear,
            @RequestParam(required = false, defaultValue = "") String type,
            HttpSession session,
            Model model) {

        User user = getLoggedIn(session);

        // Run search through filter chain
        model.addAttribute("results",  searchService.search(keyword, brand, minPrice, maxPrice, fromYear, toYear, type));
        model.addAttribute("filters",  searchService.describeFilters(keyword, brand, minPrice, maxPrice, fromYear, toYear, type));
        model.addAttribute("brands",   searchService.getDistinctBrands());

        // Saved preferences for the logged-in user
        if (user != null) {
            model.addAttribute("savedPrefs", searchService.getPreferencesForUser(user.getUserId()));
        }

        // Carry form values back to view
        model.addAttribute("keyword",  keyword);
        model.addAttribute("brand",    brand);
        model.addAttribute("minPrice", minPrice > 0 ? minPrice : "");
        model.addAttribute("maxPrice", maxPrice > 0 ? maxPrice : "");
        model.addAttribute("fromYear", fromYear > 0 ? fromYear : "");
        model.addAttribute("toYear",   toYear   > 0 ? toYear   : "");
        model.addAttribute("type",     type);

        return "search/search";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SAVE PREFERENCE  (Create)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/save")
    public String savePreference(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String brand,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false, defaultValue = "0") double maxPrice,
            @RequestParam(required = false, defaultValue = "0") int fromYear,
            @RequestParam(required = false, defaultValue = "0") int toYear,
            @RequestParam(required = false, defaultValue = "") String type,
            HttpSession session,
            RedirectAttributes ra) {

        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";

        SearchPreference pref = new SearchPreference(
                null, user.getUserId(), keyword, brand,
                minPrice, maxPrice, fromYear, toYear, type);
        searchService.savePreference(pref);
        ra.addFlashAttribute("success", "Search preference saved!");
        return buildRedirect(keyword, brand, minPrice, maxPrice, fromYear, toYear, type);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DELETE PREFERENCE  (Delete one)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/prefs/{prefId}/delete")
    public String deletePreference(@PathVariable String prefId,
                                   HttpSession session, RedirectAttributes ra) {
        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";
        searchService.deletePreference(prefId);
        ra.addFlashAttribute("success", "Saved search removed.");
        return "redirect:/search";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CLEAR ALL PREFERENCES  (Delete all)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/prefs/clear")
    public String clearPreferences(HttpSession session, RedirectAttributes ra) {
        User user = getLoggedIn(session);
        if (user == null) return "redirect:/login";
        searchService.clearPreferencesForUser(user.getUserId());
        ra.addFlashAttribute("success", "All saved searches cleared.");
        return "redirect:/search";
    }

    // ─── Helper: rebuild redirect URL with current filters ────────────────────
    private String buildRedirect(String keyword, String brand,
                                 double minPrice, double maxPrice,
                                 int fromYear, int toYear, String type) {
        return String.format(
            "redirect:/search?keyword=%s&brand=%s&minPrice=%.0f&maxPrice=%.0f&fromYear=%d&toYear=%d&type=%s",
            encode(keyword), encode(brand), minPrice, maxPrice, fromYear, toYear, encode(type));
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (Exception e) { return ""; }
    }
}
