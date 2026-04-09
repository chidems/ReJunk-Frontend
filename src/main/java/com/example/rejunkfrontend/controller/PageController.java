package com.example.rejunkfrontend.controller;

import com.example.rejunkfrontend.client.BackendClient;
import com.example.rejunkfrontend.dto.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    private final BackendClient backendClient;

    public PageController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    private AuthResponse getSessionUser(HttpSession session) {
        return (AuthResponse) session.getAttribute("user");
    }

    // Public pages

    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password,
                          HttpSession session, RedirectAttributes ra) {
        try {
            AuthResponse user = backendClient.login(new LoginRequest(email, password));
            session.setAttribute("user", user);
            log.info("Login: user={} role='{}'", user.email(), user.role());
            if (user.role() != null && user.role().toUpperCase().contains("ADMIN")) {
                return "redirect:/admin";
            }
            return "redirect:/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String name, @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam String password,
                             HttpSession session, RedirectAttributes ra) {
        try {
            AuthResponse user = backendClient.register(new RegisterRequest(name, email, phone, password));
            session.setAttribute("user", user);
            ra.addFlashAttribute("success", "Account created! Welcome to ReJunk.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // Marketplace

    @GetMapping("/marketplace")
    public String marketplace(HttpSession session, Model model) {
        if (getSessionUser(session) == null) return "redirect:/login";
        try {
            model.addAttribute("items", backendClient.getActiveListings());
        } catch (Exception e) {
            model.addAttribute("items", List.of());
            model.addAttribute("error", "Could not load listings: " + e.getMessage());
        }
        model.addAttribute("filters", List.of());
        model.addAttribute("categories", List.of());
        model.addAttribute("activePage", "marketplace");
        return "marketplace/browse";
    }

    @GetMapping("/marketplace/{id}")
    public String itemDetail(@PathVariable UUID id, HttpSession session, Model model) {
        if (getSessionUser(session) == null) return "redirect:/login";
        try {
            model.addAttribute("item", backendClient.getListing(id));
        } catch (Exception e) {
            try {
                model.addAttribute("item", backendClient.getItem(id));
            } catch (Exception e2) {
                model.addAttribute("error", "Could not load item.");
            }
        }
        return "marketplace/item-detail";
    }

    @GetMapping("/marketplace/{id}/buy")
    public String checkout(@PathVariable UUID id, HttpSession session, Model model) {
        if (getSessionUser(session) == null) return "redirect:/login";
        try {
            model.addAttribute("item", backendClient.getListing(id));
        } catch (Exception e) {
            model.addAttribute("error", "Could not load item: " + e.getMessage());
        }
        return "marketplace/checkout";
    }

    @PostMapping("/marketplace/{id}/buy")
    public String doCheckout(@PathVariable UUID id, HttpSession session, RedirectAttributes ra) {
        AuthResponse user = getSessionUser(session);
        if (user == null) {
            ra.addFlashAttribute("error", "Please log in to complete your purchase.");
            return "redirect:/login";
        }
        try {
            UUID buyerId = UUID.fromString(user.userId());
            backendClient.createOrder(new CreateOrderRequest(buyerId, List.of(id)));
            ra.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/marketplace/order-tracking";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not place order: " + e.getMessage());
            return "redirect:/marketplace/" + id + "/buy";
        }
    }

    @GetMapping("/marketplace/order-tracking")
    public String orderTracking(HttpSession session, Model model) {
        AuthResponse user = getSessionUser(session);
        if (user == null) {
            model.addAttribute("orders", List.of());
            return "marketplace/order-tracking";
        }
        try {
            UUID userId = UUID.fromString(user.userId());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            List<OrderDto> rawOrders = backendClient.getOrdersByBuyer(userId);
            List<OrderView> orders = new ArrayList<>();
            for (OrderDto order : rawOrders) {
                String formatted = order.createdAt() != null ? fmt.format(order.createdAt()) : "";
                orders.add(new OrderView(order.id(), order.totalAmount(), order.orderStatus(), formatted, order.items()));
            }
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }
        return "marketplace/order-tracking";
    }

    // Customer pages

    @GetMapping("/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        AuthResponse user = getSessionUser(session);
        if (user == null) return "redirect:/login";
        UUID userId = UUID.fromString(user.userId());
        try {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
            List<CollectionRequestDto> allRequests = backendClient.getCollectionRequestsByUser(userId);
            List<DashboardRequestView> recentRequests = new ArrayList<>();
            for (int i = 0; i < Math.min(5, allRequests.size()); i++) {
                CollectionRequestDto r = allRequests.get(i);
                String date = r.preferredPickupTime() != null ? dateFmt.format(r.preferredPickupTime()) : "";
                String time = r.preferredPickupTime() != null ? timeFmt.format(r.preferredPickupTime()) : "";
                String address = r.pickupAddress() != null ? r.pickupAddress() : "";
                int itemCount = r.items() != null ? r.items().size() : 0;
                recentRequests.add(new DashboardRequestView(r.id(), date, time, address, itemCount, r.requestStatus()));
            }
            model.addAttribute("recentRequests", recentRequests);
        } catch (Exception e) {
            model.addAttribute("recentRequests", List.of());
        }
        try {
            DateTimeFormatter notifFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            List<NotificationView> notifs = new ArrayList<>();
            for (NotificationDto n : backendClient.getNotificationsByUser(userId)) {
                String formatted = n.createdAt() != null ? notifFmt.format(n.createdAt()) : "";
                notifs.add(new NotificationView(n.id(), n.type(), n.message(), n.read(), formatted));
            }
            model.addAttribute("notifications", notifs);
        } catch (Exception e) {
            model.addAttribute("notifications", List.of());
        }
        model.addAttribute("activePage", "dashboard");
        return "customer/dashboard";
    }

    @GetMapping("/collections")
    public String collections(HttpSession session, Model model) {
        AuthResponse user = getSessionUser(session);
        if (user == null) return "redirect:/login";
        try {
            UUID userId = UUID.fromString(user.userId());
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
            List<DashboardRequestView> collections = new ArrayList<>();
            for (CollectionRequestDto r : backendClient.getCollectionRequestsByUser(userId)) {
                String date = r.preferredPickupTime() != null ? dateFmt.format(r.preferredPickupTime()) : "";
                String time = r.preferredPickupTime() != null ? timeFmt.format(r.preferredPickupTime()) : "";
                String address = r.pickupAddress() != null ? r.pickupAddress() : "";
                int itemCount = r.items() != null ? r.items().size() : 0;
                collections.add(new DashboardRequestView(r.id(), date, time, address, itemCount, r.requestStatus()));
            }
            model.addAttribute("collections", collections);
        } catch (Exception e) {
            model.addAttribute("collections", List.of());
            model.addAttribute("error", "Could not load collections: " + e.getMessage());
        }
        model.addAttribute("activePage", "requests");
        return "customer/collections";
    }

    @GetMapping("/my-items")
    public String myItems(HttpSession session, Model model) {
        AuthResponse user = getSessionUser(session);
        if (user == null) return "redirect:/login";
        try {
            UUID userId = UUID.fromString(user.userId());
            java.util.Set<UUID> userItemIds = new java.util.HashSet<>();
            for (CollectionRequestDto r : backendClient.getCollectionRequestsByUser(userId)) {
                if (r.items() != null) {
                    r.items().forEach(item -> userItemIds.add(item.id()));
                }
            }
            List<ListingDto> listings = backendClient.getAllListings().stream()
                    .filter(l -> userItemIds.contains(l.item().id()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("items", listings);
        } catch (Exception e) {
            model.addAttribute("items", List.of());
            model.addAttribute("error", "Could not load items: " + e.getMessage());
        }
        model.addAttribute("activePage", "items");
        return "customer/my-items";
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        AuthResponse user = getSessionUser(session);
        if (user == null) return "redirect:/login";
        try {
            UUID userId = UUID.fromString(user.userId());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withZone(ZoneId.systemDefault());
            List<NotificationView> notifications = new ArrayList<>();
            for (NotificationDto n : backendClient.getNotificationsByUser(userId)) {
                String formatted = n.createdAt() != null ? fmt.format(n.createdAt()) : "";
                notifications.add(new NotificationView(n.id(), n.type(), n.message(), n.read(), formatted));
            }
            model.addAttribute("notifications", notifications);
        } catch (Exception e) {
            model.addAttribute("notifications", List.of());
            model.addAttribute("error", "Could not load notifications: " + e.getMessage());
        }
        model.addAttribute("activePage", "notifications");
        return "customer/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            backendClient.markNotificationRead(id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not mark notification as read.");
        }
        return "redirect:/notifications";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        if (getSessionUser(session) == null) return "redirect:/login";
        return "customer/profile";
    }

    @PostMapping("/profile/address")
    public String updateAddress(@RequestParam String address, HttpSession session, RedirectAttributes ra) {
        session.setAttribute("profileAddress", address);
        ra.addFlashAttribute("success", "Address saved.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam MultipartFile avatar, HttpSession session, RedirectAttributes ra) {
        if (!avatar.isEmpty()) {
            try {
                String base64 = Base64.getEncoder().encodeToString(avatar.getBytes());
                String dataUrl = "data:" + avatar.getContentType() + ";base64," + base64;
                session.setAttribute("avatarUrl", dataUrl);
                ra.addFlashAttribute("success", "Profile picture updated.");
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Could not update avatar: " + e.getMessage());
            }
        }
        return "redirect:/profile";
    }

    @GetMapping("/collections/new")
    public String collectionRequest(HttpSession session) {
        if (getSessionUser(session) == null) return "redirect:/login";
        return "customer/collection-request";
    }

    @PostMapping("/collections/new")
    public String doCreateCollection(@RequestParam String address,
                                     @RequestParam(required = false) String city,
                                     @RequestParam(required = false) String preferredDate,
                                     @RequestParam(required = false) String preferredTime,
                                     HttpSession session, RedirectAttributes ra) {
        AuthResponse user = getSessionUser(session);
        if (user == null) return "redirect:/login";
        try {
            String fullAddress = address;
            if (city != null && !city.isBlank()) {
                fullAddress = address + ", " + city;
            }
            Instant pickupTime = null;
            if (preferredDate != null && !preferredDate.isBlank() && preferredTime != null && !preferredTime.isBlank()) {
                pickupTime = Instant.parse(preferredDate + "T" + preferredTime + ":00Z");
            }
            UUID customerId = UUID.fromString(user.userId());
            backendClient.createCollectionRequest(new CreateCollectionRequestRequest(customerId, fullAddress, pickupTime, BigDecimal.ZERO));
            ra.addFlashAttribute("success", "Collection request submitted!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Submission failed: " + e.getMessage());
            return "redirect:/collections/new";
        }
    }

    @GetMapping("/collections/{id}")
    public String collectionDetail(@PathVariable UUID id, HttpSession session, Model model) {
        if (getSessionUser(session) == null) return "redirect:/login";
        try {
            CollectionRequestDto req = backendClient.getCollectionRequest(id);
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
            String date = req.preferredPickupTime() != null ? dateFmt.format(req.preferredPickupTime()) : "";
            String time = req.preferredPickupTime() != null ? timeFmt.format(req.preferredPickupTime()) : "";

            List<String> allStatuses = List.of("SUBMITTED", "PAID", "SCHEDULED", "COLLECTED", "EVALUATED", "CLOSED");
            List<TimelineStep> timeline = new ArrayList<>();
            if ("CANCELED".equals(req.requestStatus())) {
                timeline.add(new TimelineStep("CANCELED", "", false));
            } else {
                int currentIndex = allStatuses.indexOf(req.requestStatus());
                for (int i = 0; i < allStatuses.size(); i++) {
                    boolean pending = i > currentIndex;
                    timeline.add(new TimelineStep(allStatuses.get(i), pending ? "" : "Completed", pending));
                }
            }

            model.addAttribute("collection", new CollectionDetailView(req.id(), date, req.requestStatus(), req.pickupAddress(), date, time, timeline));
        } catch (Exception e) {
            model.addAttribute("error", "Could not load collection request: " + e.getMessage());
        }
        return "customer/collection-detail";
    }

    // Admin pages

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        try {
            List<UserDto> users = backendClient.getAllUsers();
            int activeCount = 0;
            int suspendedCount = 0;
            int customerCount = 0;
            for (UserDto u : users) {
                if ("ACTIVE".equals(u.status())) activeCount++;
                if ("SUSPENDED".equals(u.status())) suspendedCount++;
                if ("CUSTOMER".equals(u.role())) customerCount++;
            }
            model.addAttribute("users", users);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("activeUsers", activeCount);
            model.addAttribute("suspendedUsers", suspendedCount);
            model.addAttribute("customerCount", customerCount);
        } catch (Exception e) {
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("activeUsers", 0);
            model.addAttribute("suspendedUsers", 0);
            model.addAttribute("customerCount", 0);
            model.addAttribute("error", "Could not load users: " + e.getMessage());
        }
        model.addAttribute("activePage", "admin-dashboard");
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
        model.addAttribute("activePage", "add-items");
        return "admin/add-items";
    }

    @PostMapping("/admin/collection-requests/{id}/items")
    public String addItemToCollection(@PathVariable UUID id,
                                      @RequestParam UUID customerId,
                                      @RequestParam String title,
                                      @RequestParam(required = false) String description,
                                      @RequestParam(required = false) String condition,
                                      RedirectAttributes ra) {
        try {
            backendClient.createItem(new CreateItemRequest(customerId, id, title, description, condition));
            ra.addFlashAttribute("success", "Item added to collection.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not add item: " + e.getMessage());
        }
        return "redirect:/admin/add-items";
    }

    @PostMapping("/admin/collection-requests/{id}/status")
    public String updateCollectionRequestStatus(@PathVariable UUID id, @RequestParam String status, RedirectAttributes ra) {
        try {
            backendClient.updateCollectionRequestStatus(id, status);
            ra.addFlashAttribute("success", "Request status updated to " + status + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not update status: " + e.getMessage());
        }
        return "redirect:/admin/add-items";
    }

    @GetMapping("/admin/add-item")
    public String addItem(@RequestParam(required = false) UUID itemId, Model model) {
        if (itemId != null) {
            try {
                model.addAttribute("item", backendClient.getItem(itemId));
            } catch (Exception e) {
                model.addAttribute("error", "Could not load item: " + e.getMessage());
            }
        }
        return "admin/add-item";
    }

    @PostMapping("/admin/add-item")
    public String doAddItem(@RequestParam UUID itemId, @RequestParam String condition,
                            @RequestParam BigDecimal price, RedirectAttributes ra) {
        try {
            backendClient.evaluateItem(itemId, new EvaluateItemRequest(condition, price));
            backendClient.createListing(new CreateListingRequest(itemId, price));
            ra.addFlashAttribute("success", "Item evaluated and listed on the marketplace.");
            return "redirect:/admin/listings";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not list item: " + e.getMessage());
            return "redirect:/admin/add-item?itemId=" + itemId;
        }
    }

    @GetMapping("/admin/customers")
    public String adminCustomers(Model model) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            List<CustomerView> customers = new ArrayList<>();
            for (UserDto u : backendClient.getCustomers()) {
                String createdAt = u.createdAt() != null ? fmt.format(u.createdAt()) : "";
                customers.add(new CustomerView(u.id(), u.fullName(), u.email(), u.phone(), u.status(), u.role(), createdAt));
            }
            model.addAttribute("customers", customers);
        } catch (Exception e) {
            model.addAttribute("customers", List.of());
            model.addAttribute("error", "Could not load customers: " + e.getMessage());
        }
        model.addAttribute("activePage", "customers");
        return "admin/customers";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());
            List<OrderDto> rawOrders = backendClient.getAllOrders();
            List<OrderView> orders = new ArrayList<>();
            for (OrderDto order : rawOrders) {
                String formatted = order.createdAt() != null ? fmt.format(order.createdAt()) : "";
                orders.add(new OrderView(order.id(), order.totalAmount(), order.orderStatus(), formatted, order.items()));
            }
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }
        model.addAttribute("activePage", "orders");
        return "admin/orders";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateAdminOrderStatus(@PathVariable UUID id, @RequestParam String status, RedirectAttributes ra) {
        try {
            backendClient.updateOrderStatus(id, new UpdateOrderStatusRequest(status));
            ra.addFlashAttribute("success", "Order status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not update order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/admin/listings")
    public String listings(Model model) {
        try {
            model.addAttribute("listings", backendClient.getAllListings());
        } catch (Exception e) {
            model.addAttribute("listings", List.of());
            model.addAttribute("error", "Could not load listings: " + e.getMessage());
        }
        model.addAttribute("activePage", "listings");
        return "admin/listings";
    }

    @PostMapping("/admin/listings/{id}/delete")
    public String deleteListing(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            backendClient.removeListing(id);
            ra.addFlashAttribute("success", "Listing removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not remove listing: " + e.getMessage());
        }
        return "redirect:/admin/listings";
    }

    @PostMapping("/admin/users/{id}/suspend")
    public String suspendUser(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            backendClient.suspendUser(id);
            ra.addFlashAttribute("success", "User suspended.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not suspend user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/activate")
    public String activateUser(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            backendClient.activateUser(id);
            ra.addFlashAttribute("success", "User activated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not activate user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            backendClient.deleteUser(id);
            ra.addFlashAttribute("success", "User deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not delete user: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}