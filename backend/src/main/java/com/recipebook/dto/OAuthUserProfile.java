package com.recipebook.dto;

public record OAuthUserProfile(
        String providerId,
        String email,
        String name,
        String avatarUrl
) {}
