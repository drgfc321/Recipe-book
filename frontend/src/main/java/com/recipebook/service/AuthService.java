package com.recipebook.service;

import com.recipebook.dto.AuthResponse;
import com.recipebook.dto.LoginRequest;
import com.recipebook.dto.RegisterRequest;
import com.recipebook.dto.UserInfo;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
public class AuthService {

    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AuthResponse login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        AuthResponse response = apiClient.postPublic("/api/auth/login", request, AuthResponse.class);
        storeToken(response.token());
        storeUserInfo(response);
        return response;
    }

    public AuthResponse register(String email, String username, String password, String language) {
        RegisterRequest request = new RegisterRequest(email, username, password, language);
        AuthResponse response = apiClient.postPublic("/api/auth/register", request, AuthResponse.class);
        storeToken(response.token());
        storeUserInfo(response);
        return response;
    }

    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("jwt_token", null);
            session.setAttribute("user_info", null);
        }
    }

    public boolean isLoggedIn() {
        return getToken().isPresent();
    }

    public Optional<String> getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object token = session.getAttribute("jwt_token");
            if (token != null) {
                return Optional.of(token.toString());
            }
        }
        return Optional.empty();
    }

    public Optional<UserInfo> getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object userInfo = session.getAttribute("user_info");
            if (userInfo instanceof UserInfo) {
                return Optional.of((UserInfo) userInfo);
            }
        }
        return Optional.empty();
    }

    public UserInfo fetchCurrentUser() {
        try {
            return apiClient.get("/api/auth/me", UserInfo.class);
        } catch (WebClientResponseException.Unauthorized e) {
            logout();
            return null;
        }
    }

    private void storeToken(String token) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("jwt_token", token);
        }
    }

    private void storeUserInfo(AuthResponse response) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            UserInfo userInfo = new UserInfo(
                    response.userId(),
                    response.username(),
                    response.email(),
                    response.role()
            );
            session.setAttribute("user_info", userInfo);
        }
    }
}
