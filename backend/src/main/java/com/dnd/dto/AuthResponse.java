package com.dnd.dto;

public class AuthResponse {

    public String token;
    public String type = "Bearer";
    public Long userId;
    public String username;
    public String email;
    public String role;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String username, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
