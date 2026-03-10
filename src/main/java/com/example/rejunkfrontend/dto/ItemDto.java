package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemDto(
        UUID id,
        String title,
        String description,
        String condition,
        String evaluationStatus,
        BigDecimal evaluatedPrice,
        BigDecimal price,
        String listingStatus,
        String imageUrl,
        String category
) {}
