package com.example.rejunkfrontend.dto;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String fullName,
        String email,
        String phone,
        String role,        // UserRole enum: CUSTOMER, ADMIN, etc.
        String status,      // AccountStatus enum: ACTIVE, SUSPENDED, etc.
        Instant createdAt
) {}
