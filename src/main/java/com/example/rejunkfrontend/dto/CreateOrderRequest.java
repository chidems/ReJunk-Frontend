package com.example.rejunkfrontend.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(UUID buyerId, List<UUID> listingIds) {}