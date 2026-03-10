package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CollectionRequestDto(
        UUID id,
        UUID customerId,
        String pickupAddress,
        Instant preferredPickupTime,
        BigDecimal pickupFee,
        String paymentStatus,
        String requestStatus,
        List<ItemDto> items
) {}
