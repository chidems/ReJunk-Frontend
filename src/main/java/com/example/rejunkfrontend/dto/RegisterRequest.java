package com.example.rejunkfrontend.dto;

public record RegisterRequest(
        String name,
        String email,
        String phone,
        String password
) {}
