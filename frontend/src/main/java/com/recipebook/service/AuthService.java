package com.recipebook.service;

import com.recipebook.dto.AuthResponse;
import com.recipebook.dto.UserInfo;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AuthResponse login(String email, String password) {
        String query = """
                mutation($email: String!, $password: String!) {
                    login(email: $email, password: $password) {
                        token userId username email role
                    }
                }
                """;
        AuthResponse response = apiClient.mutatePublic(query,
                Map.of("email", email, "password", password),
                AuthResponse.class, "login");
        storeToken(response.token());
        storeUserInfo(response);
        return response;
    }

    public AuthResponse register(String email, String username, String password, String language) {
        String query = """
                mutation($email: String!, $username: String!, $password: String!, $language: String!) {
                    register(email: $email, username: $username, password: $password, language: $language) {
                        token userId username email role
                    }
                }
                """;
        AuthResponse response = apiClient.mutatePublic(query,
                Map.of("email", email, "username", username, "password", password, "language", language),
                AuthResponse.class, "register");
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
            String query = """
                    query {
                        me { id username email role }
                    }
                    """;
            return apiClient.query(query, null, UserInfo.class, "me");
        } catch (RuntimeException e) {
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
