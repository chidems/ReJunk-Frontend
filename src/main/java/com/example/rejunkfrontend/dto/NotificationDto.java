package com.example.rejunkfrontend.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String type,
        String message,
        boolean read,
        Instant createdAt
) {}
