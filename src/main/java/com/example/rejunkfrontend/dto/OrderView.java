package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderView(
        UUID id,
        BigDecimal totalAmount,
        String orderStatus,
        String createdAtFormatted,
        List<OrderItemDto> items
) {}