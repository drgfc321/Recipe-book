package com.recipebook.dto;

import java.io.Serializable;

public record UserInfo(
        Long id,
        String username,
        String email,
        String role,
        String avatarUrl,
        String authProvider
) implements Serializable {}
