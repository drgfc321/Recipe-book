package com.recipebook.dto;

public record OAuthConfig(
        boolean googleEnabled,
        boolean githubEnabled
) {}
