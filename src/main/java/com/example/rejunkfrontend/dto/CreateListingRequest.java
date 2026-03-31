package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateListingRequest(UUID itemId, BigDecimal price) {}