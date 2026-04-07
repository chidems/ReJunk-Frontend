package com.example.rejunkfrontend.controller;

import com.example.rejunkfrontend.client.BackendClient;
import com.example.rejunkfrontend.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    private BackendClient backendClient;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PageController pageController;

    private MockHttpSession session;
    private AuthResponse customerUser;
    private AuthResponse adminUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        customerUser = new AuthResponse(null, UUID.randomUUID().toString(), "Jane Doe", "jane@example.com", "CUSTOMER", null);
        adminUser = new AuthResponse(null, UUID.randomUUID().toString(), "Admin User", "admin@example.com", "ADMIN", null);
    }

    // LOGIN

    @Test
    void doLogin_customerRole_redirectsToDashboard() {
        when(backendClient.login(any(LoginRequest.class))).thenReturn(customerUser);

        String result = pageController.doLogin("jane@example.com", "password", session, redirectAttributes);

        assertEquals("redirect:/dashboard", result);
    }

    @Test
    void doLogin_adminRole_redirectsToAdmin() {
        when(backendClient.login(any(LoginRequest.class))).thenReturn(adminUser);

        String result = pageController.doLogin("admin@example.com", "password", session, redirectAttributes);

        assertEquals("redirect:/admin", result);
    }

    @Test
    void doLogin_invalidCredentials_redirectsBackToLogin() {
        when(backendClient.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Unauthorized"));

        String result = pageController.doLogin("bad@example.com", "wrongpass", session, redirectAttributes);

        assertEquals("redirect:/login", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
    }

    // MARKETPLACE

    @Test
    void marketplace_backendAvailable_addsItemsToModel() {
        session.setAttribute("user", customerUser);
        ItemDto item = new ItemDto(UUID.randomUUID(), "Old Lamp", "A used lamp", "GOOD", null, null, BigDecimal.valueOf(25.00), null, null, null);
        ListingDto listing = new ListingDto(UUID.randomUUID(), item, BigDecimal.valueOf(25.00), "ACTIVE");
        when(backendClient.getActiveListings()).thenReturn(List.of(listing));

        String view = pageController.marketplace(session, model);

        assertEquals("marketplace/browse", view);
        verify(model).addAttribute("items", List.of(listing));
    }

    @Test
    void marketplace_backendFails_addsEmptyListAndError() {
        session.setAttribute("user", customerUser);
        when(backendClient.getActiveListings()).thenThrow(new RuntimeException("Service unavailable"));

        String view = pageController.marketplace(session, model);

        assertEquals("marketplace/browse", view);
        verify(model).addAttribute(eq("items"), eq(List.of()));
        verify(model).addAttribute(eq("error"), any());
    }

    // AUTH

    @Test
    void dashboard_noSession_redirectsToLogin() {
        String result = pageController.customerDashboard(session, model);

        assertEquals("redirect:/login", result);
        verifyNoInteractions(backendClient);
    }

    @Test
    void checkout_noSession_redirectsToLogin() {
        UUID itemId = UUID.randomUUID();

        String result = pageController.doCheckout(itemId, session, redirectAttributes);

        assertEquals("redirect:/login", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
    }

    @Test
    void checkout_loggedIn_placesOrderAndRedirects() {
        session.setAttribute("user", customerUser);
        UUID itemId = UUID.randomUUID();

        String result = pageController.doCheckout(itemId, session, redirectAttributes);

        assertEquals("redirect:/marketplace/order-tracking", result);
        verify(backendClient).createOrder(any());
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
    }

    // ADD ITEM TO COLLECTION

    @Test
    void addItemToCollection_success_redirectsToAddItems() {
        UUID collectionId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(backendClient.createItem(any(CreateItemRequest.class)))
                .thenReturn(new ItemDto(UUID.randomUUID(), "Oak Chair", null, null, null, null, null, null, null, null));

        String result = pageController.addItemToCollection(
                collectionId, customerId, "Oak Chair", "Optional notes", "Chair", redirectAttributes);

        assertEquals("redirect:/admin/add-items", result);
        verify(backendClient).createItem(any(CreateItemRequest.class));
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
    }

    @Test
    void addItemToCollection_backendFails_setsErrorFlash() {
        UUID collectionId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(backendClient.createItem(any(CreateItemRequest.class)))
                .thenThrow(new RuntimeException("Backend error"));

        String result = pageController.addItemToCollection(
                collectionId, customerId, "Oak Chair", null, null, redirectAttributes);

        assertEquals("redirect:/admin/add-items", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
    }
}
