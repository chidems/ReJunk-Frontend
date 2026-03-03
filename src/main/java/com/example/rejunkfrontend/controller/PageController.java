package com.example.rejunkfrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    // Public
    @GetMapping("/")
    public String landing() { return "landing"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    // Marketplace
    @GetMapping("/marketplace")
    public String marketplace() { return "marketplace/browse"; }

    @GetMapping("/marketplace/{id}")
    public String itemDetail(@PathVariable String id) { return "marketplace/item-detail"; }

    @GetMapping("/marketplace/{id}/buy")
    public String checkout(@PathVariable String id) { return "marketplace/checkout"; }

    @GetMapping("/marketplace/order-tracking")
    public String orderTracking() { return "marketplace/order-tracking"; }

    // Customer
    @GetMapping("/dashboard")
    public String customerDashboard() { return "customer/dashboard"; }

    @GetMapping("/collections/new")
    public String collectionRequest() { return "customer/collection-request"; }

    @GetMapping("/collections/{id}")
    public String collectionDetail(@PathVariable String id) { return "customer/collection-detail"; }

    // Admin
    @GetMapping("/admin")
    public String adminDashboard() { return "admin/dashboard"; }

    @GetMapping("/admin/add-items")
    public String addItems() { return "admin/add-items"; }

    @GetMapping("/admin/add-item")
    public String addItem() { return "admin/add-item"; }

    @GetMapping("/admin/listings")
    public String listings() { return "admin/listings"; }
}
