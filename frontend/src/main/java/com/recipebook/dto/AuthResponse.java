package com.recipebook.dto;

public record AuthResponse(
        String token,
        String type,
        Long userId,
        String username,
        String email,
        String role,
        String avatarUrl,
        String authProvider
) {}
