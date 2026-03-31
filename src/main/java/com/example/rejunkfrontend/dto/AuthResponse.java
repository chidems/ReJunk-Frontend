package com.example.rejunkfrontend.dto;

public record AuthResponse(String userId, String fullName, String email, String role, String message) {}