package com.example.rejunkfrontend.dto;

public record AuthResponse(String token, String userId, String fullName, String email, String role, String message) {}