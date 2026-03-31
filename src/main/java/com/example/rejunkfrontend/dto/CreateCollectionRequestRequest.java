package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCollectionRequestRequest(
        UUID customerId,
        String pickupAddress,
        Instant preferredPickupTime,
        BigDecimal pickupFee
) {}