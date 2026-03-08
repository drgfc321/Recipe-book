package com.recipebook.dto;

public class AuthResponse {

    public String token;
    public String type = "Bearer";
    public Long userId;
    public String username;
    public String email;
    public String role;
    public String avatarUrl;
    public String authProvider;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String username, String email, String role, String avatarUrl, String authProvider) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.authProvider = authProvider;
    }
}
