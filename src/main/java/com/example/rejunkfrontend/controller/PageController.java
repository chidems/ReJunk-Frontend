package com.example.rejunkfrontend.controller;

import com.example.rejunkfrontend.client.BackendClient;
import com.example.rejunkfrontend.dto.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class PageController {

    private final BackendClient backendClient;

    public PageController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    // --- Public ---

    @GetMapping("/")
    public String landing() { return "landing"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String password,
            RedirectAttributes ra
    ) {
        try {
            backendClient.registerUser(new RegisterRequest(name, email, phone, password));
            ra.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // --- Marketplace ---

    @GetMapping("/marketplace")
    public String marketplace(Model model) {
        return "marketplace/browse";
    }

    @GetMapping("/marketplace/{id}")
    public String itemDetail(@PathVariable String id, Model model) {
        return "marketplace/item-detail";
    }

    @GetMapping("/marketplace/{id}/buy")
    public String checkout(@PathVariable String id) { return "marketplace/checkout"; }

    @GetMapping("/marketplace/order-tracking")
    public String orderTracking() { return "marketplace/order-tracking"; }

    // --- Customer ---

    @GetMapping("/dashboard")
    public String customerDashboard() { return "customer/dashboard"; }

    @GetMapping("/collections/new")
    public String collectionRequest() { return "customer/collection-request"; }

    @GetMapping("/collections/{id}")
    public String collectionDetail(@PathVariable String id) { return "customer/collection-detail"; }

    // --- Admin ---

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("users", backendClient.getAllUsers());
        return "admin/dashboard";
    }

    @GetMapping("/admin/add-items")
    public String addItems() { return "admin/add-items"; }

    @GetMapping("/admin/add-item")
    public String addItem() { return "admin/add-item"; }

    @GetMapping("/admin/listings")
    public String listings(Model model) {
        model.addAttribute("customers", backendClient.getCustomers());
        return "admin/listings";
    }

    @PostMapping("/admin/users/{id}/suspend")
    public String suspendUser(@PathVariable UUID id, RedirectAttributes ra) {
        backendClient.suspendUser(id);
        ra.addFlashAttribute("success", "User suspended.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/activate")
    public String activateUser(@PathVariable UUID id, RedirectAttributes ra) {
        backendClient.activateUser(id);
        ra.addFlashAttribute("success", "User activated.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable UUID id, RedirectAttributes ra) {
        backendClient.deleteUser(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin";
    }
}
