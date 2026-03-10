package com.example.rejunkfrontend.controller;

import com.example.rejunkfrontend.client.BackendClient;
import com.example.rejunkfrontend.dto.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class PageController {

    private final BackendClient backendClient;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    private static final List<String> TIMELINE_STATUSES =
            List.of("SUBMITTED", "PAID", "SCHEDULED", "COLLECTED", "EVALUATED", "CLOSED");

    public PageController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

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

    @GetMapping("/marketplace")
    public String marketplace(Model model) {
        try {
            model.addAttribute("items", backendClient.getActiveListings());
        } catch (Exception e) {
            model.addAttribute("items", List.of());
            model.addAttribute("error", "Could not load listings: " + e.getMessage());
        }
        model.addAttribute("filters", List.of());
        model.addAttribute("categories", List.of());
        return "marketplace/browse";
    }

    @GetMapping("/marketplace/{id}")
    public String itemDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("item", backendClient.getListing(id));
        return "marketplace/item-detail";
    }

    @GetMapping("/marketplace/{id}/buy")
    public String checkout(@PathVariable UUID id, Model model) {
        model.addAttribute("item", backendClient.getListing(id));
        return "marketplace/checkout";
    }

    @GetMapping("/marketplace/order-tracking")
    public String orderTracking(Model model) {
        // TODO: load orders for logged-in user once auth is implemented
        model.addAttribute("orders", List.of());
        return "marketplace/order-tracking";
    }

    @GetMapping("/dashboard")
    public String customerDashboard(Model model) {
        // TODO: load data for logged-in user once auth is implemented
        model.addAttribute("recentRequests", List.of());
        model.addAttribute("notifications", List.of());
        return "customer/dashboard";
    }

    @GetMapping("/collections")
    public String collections(Model model) {
        // TODO: load for logged-in user once auth is implemented
        model.addAttribute("collections", List.of());
        return "customer/collections";
    }

    @GetMapping("/my-items")
    public String myItems(Model model) {
        // TODO: load for logged-in user once auth is implemented
        model.addAttribute("items", List.of());
        return "customer/my-items";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        // TODO: load for logged-in user once auth is implemented
        model.addAttribute("notifications", List.of());
        return "customer/notifications";
    }

    @GetMapping("/collections/new")
    public String collectionRequest() { return "customer/collection-request"; }

    @PostMapping("/collections/new")
    public String doCreateCollection(
            @RequestParam String address,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String preferredDate,
            @RequestParam(required = false) String preferredTime,
            RedirectAttributes ra
    ) {
        try {
            String fullAddress = (city != null && !city.isBlank()) ? address + ", " + city : address;
            String isoDateTime = (preferredDate != null && preferredTime != null)
                    ? preferredDate + "T" + preferredTime + ":00Z"
                    : null;
            backendClient.createCollectionRequest(new CollectionRequestForm(fullAddress, isoDateTime));
            ra.addFlashAttribute("success", "Collection request submitted!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Submission failed: " + e.getMessage());
            return "redirect:/collections/new";
        }
    }

    @GetMapping("/collections/{id}")
    public String collectionDetail(@PathVariable UUID id, Model model) {
        CollectionRequestDto req = backendClient.getCollectionRequest(id);
        model.addAttribute("collection", toDetailView(req));
        return "customer/collection-detail";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        try {
            List<UserDto> users = backendClient.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("activeUsers", users.stream().filter(u -> "ACTIVE".equals(u.status())).count());
            model.addAttribute("suspendedUsers", users.stream().filter(u -> "SUSPENDED".equals(u.status())).count());
            model.addAttribute("customerCount", users.stream().filter(u -> "CUSTOMER".equals(u.role())).count());
        } catch (Exception e) {
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("activeUsers", 0);
            model.addAttribute("suspendedUsers", 0);
            model.addAttribute("customerCount", 0);
            model.addAttribute("error", "Could not load users: " + e.getMessage());
        }
        return "admin/dashboard";
    }

    @GetMapping("/admin/add-items")
    public String addItems(Model model) {
        try {
            model.addAttribute("requests", backendClient.getAllCollectionRequests());
        } catch (Exception e) {
            model.addAttribute("requests", List.of());
            model.addAttribute("error", "Could not load collection requests: " + e.getMessage());
        }
        return "admin/add-items";
    }

    @GetMapping("/admin/add-item")
    public String addItem() { return "admin/add-item"; }

    @GetMapping("/admin/customers")
    public String adminCustomers(Model model) {
        try {
            List<CustomerView> customers = backendClient.getCustomers().stream()
                    .map(u -> new CustomerView(
                            u.id(), u.fullName(), u.email(), u.phone(), u.status(), u.role(),
                            u.createdAt() != null ? DATE_FMT.format(u.createdAt()) : ""))
                    .toList();
            model.addAttribute("customers", customers);
        } catch (Exception e) {
            model.addAttribute("customers", List.of());
            model.addAttribute("error", "Could not load customers: " + e.getMessage());
        }
        return "admin/customers";
    }

    @GetMapping("/admin/listings")
    public String listings(Model model) {
        model.addAttribute("listings", backendClient.getActiveListings());
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

    private CollectionDetailView toDetailView(CollectionRequestDto req) {
        String date = req.preferredPickupTime() != null ? DATE_FMT.format(req.preferredPickupTime()) : "";
        String time = req.preferredPickupTime() != null ? TIME_FMT.format(req.preferredPickupTime()) : "";
        return new CollectionDetailView(
                req.id(), date, req.requestStatus(), req.pickupAddress(), date, time,
                buildTimeline(req.requestStatus())
        );
    }

    private List<TimelineStep> buildTimeline(String currentStatus) {
        int currentIndex = TIMELINE_STATUSES.indexOf(currentStatus);
        List<TimelineStep> steps = new ArrayList<>();
        for (int i = 0; i < TIMELINE_STATUSES.size(); i++) {
            boolean pending = i > currentIndex;
            steps.add(new TimelineStep(TIMELINE_STATUSES.get(i), pending ? "" : "Completed", pending));
        }
        return steps;
    }
}
