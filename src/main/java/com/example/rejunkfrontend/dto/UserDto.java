package com.example.rejunkfrontend.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String email,
        String phone,
        String role,
        String status
) {}
