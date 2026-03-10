package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        BigDecimal totalAmount,
        String orderStatus,     // OrderStatus: PAID, PROCESSING, COMPLETED
        Instant createdAt,
        List<OrderItemDto> items
) {}
