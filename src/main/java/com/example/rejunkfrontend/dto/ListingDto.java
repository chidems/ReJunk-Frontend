package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingDto(UUID id, ItemDto item, BigDecimal price, String listingStatus) {}