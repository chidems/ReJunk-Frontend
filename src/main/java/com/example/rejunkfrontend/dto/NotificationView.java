package com.example.rejunkfrontend.dto;

import java.util.UUID;


public record NotificationView(
        UUID id,
        String type,
        String message,
        boolean read,
        String createdAtFormatted
) {}