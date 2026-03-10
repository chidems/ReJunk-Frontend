package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Flattened view of OrderItem + Listing + Item for display in the order tracking page.
public record OrderItemDto(
        UUID id,
        UUID listingId,
        String title,
        String condition,
        BigDecimal unitPrice
) {}
