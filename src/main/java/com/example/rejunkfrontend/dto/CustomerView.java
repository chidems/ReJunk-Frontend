package com.example.rejunkfrontend.dto;

import java.util.UUID;

public record CustomerView(
        UUID id,
        String fullName,
        String email,
        String phone,
        String status,
        String role,
        String createdAtFormatted
) {}
